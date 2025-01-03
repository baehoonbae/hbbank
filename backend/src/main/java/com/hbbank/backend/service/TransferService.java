package com.hbbank.backend.service;

import com.hbbank.backend.exception.account.AccountNotFoundException;
import com.hbbank.backend.exception.account.InvalidAccountPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.domain.Account;
import com.hbbank.backend.domain.enums.TransferType;
import com.hbbank.backend.dto.TransferRequestDTO;
import com.hbbank.backend.repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    private Account fromAccount, toAccount;

    /*
     * 이체 시나리오 1.
     * 단순 순차 처리
     * 1. 출금계좌 조회-> 비밀번호 검증 -> 돈 빼낸 다음 save
     * 2. 입금계좌 조회하고 돈 넣은 다음 save
     * 3. 거래내역 생성
     * 4. 종료(커밋 완료)
     * => 순차 이체 시나리오 통과
     * => 이체 시나리오 2에서 경쟁 상태(race condition) 발생 예상(잔액 부정확)
     *
     *
     * 이체 시나리오 2.
     * 동시 다중 이체 (B,C,D... -> A)
     * 순차처리 방식으로 했을 때 모든 잔액이 그대로인 현상
     * 여러 트랜잭션이 동시에 동일한 계좌에 접근할 때 충돌(race condition) 발생 예를 들어:
     * 1. 트랜잭션 A,B,C가 계좌 잔액 10000원씩 읽음
     * 2. 각각 계좌에서 1000원씩 출금하고, toAccount에 1000원씩 입금하려 시도
     * 3. 각 트랜잭션에서 개별적으로 출금 및 입금 처리가 완료되기 때문에, 각 트랜잭션에서의 계좌 잔액은 11000원이 된다.
     * 4. 따라서 입금 잔액의 최종 값은 13000원이 아닌 11000원이 되어 롤백 발생
     * 해결을 위해 동시성 제어 매커니즘(락) 필요 판단
     * 낙관적 락은 충돌이 적은 상황(충돌시 재시도)에 적합,
     * 그러나 이체는 빈번한 충돌이 예상되고 데이터 정합성이 핵심이기에 비관적 락이 적합
     * => 비관적 락을 이용해 시나리오 2 통과
     *
     *
     *
     * 이체 시나리오 3.
     * 순환 이체(A <-> B)
     * 데드락 발생 예상
     * 1. A->B 이체 시도 -> A 락 획득
     * 2. B->A 이체 시도 -> B 락 획득
     * 3. A->B 이체 시도가 B 락 획득 시도 -> B는 이미 락이 걸려있어서 대기
     * 4. B->A 이체 시도가 A 락 획득 시도 -> A는 이미 락이 걸려있어서 대기
     * 5. 서로가 서로의 락을 기다리는 데드락 상태 발생(혹은 교착 상태에서 5초 타임아웃 발생)
     * 서로 락 획득 순서가 다르기 때문에 일어나는 일
     * 모든 트랜잭션이 동일한 순서로 락을 요청하게 하면 해결. 예를 들어:
     * 1. A->B 이체 시도 -> A(락) -> B(락) -> 이체 완료 -> A,B 락 해제(이때 B->A 이체 시도 가능)
     * 2. B->A 이체 시도 -> (A 락 해제 대기) -> A(락) -> B(락) -> 이체 완료 -> A,B 락 해제
     * => 비관적 락을 적용시켜 데드락은 발생하지 않음. 그러나 B->A 이체 시도에서 변경사항을 즉시 반영하지 않는 문제 발생
     * => 첫 번째 트랜잭션이 완료되지 않았는데 두 번째 트랜잭션이 시작됨 -> 트랜잭션 격리 수준 문제
     * => 즉 다른 트랜잭션에서 잔액 검증이 실패하는 문제가 발생함..
     * => 이를 해결하기 위해 테스트 코드의 트랜잭션 격리 수준을 더 높게 설정해야 함(SERIALIZABLE)
     *
     *
     *
     * 이체 시나리오 4.
     * 체이닝 이체 (1->2, 2->3, 3->4, 4->5.... 999->1000)
     * 데드락 발생 예상
     * => 시나리오 2, 3 문제점 해결 후 통과
     *
     *
     */
    public boolean transfer(TransferRequestDTO dto) {
        String fromAccountNumber = accountRepository.findById(dto.getFromAccountId())
                .orElseThrow(() -> {
                    log.error("출금계좌 조회 실패 - 계좌ID: {}", dto.getFromAccountId());
                    return new AccountNotFoundException("출금 계좌를 찾을 수 없습니다");
                })
                .getAccountNumber();
        String toAccountNumber = dto.getToAccountNumber();

        // 계좌번호 오름차순으로 락 획득
        getLock(dto, fromAccountNumber, toAccountNumber);

        // 계좌 검증
        accountService.verifyAccount(fromAccount.getId());
        accountService.verifyAccount(toAccount.getId());

        // 비밀번호 검증
        checkPassword(dto);

        // 이체 실행
        executeTransfer(dto);

        // 거래내역 생성
        transactionService.createTransaction(fromAccount, toAccount, dto.getAmount());

        // 변경사항 저장
        accountRepository.saveAndFlush(fromAccount);
        accountRepository.saveAndFlush(toAccount);

        return true;
    }

    private void getLock(TransferRequestDTO dto, String fromAccountNumber, String toAccountNumber) {
        if (fromAccountNumber.compareTo(toAccountNumber) < 0) {
            fromAccount = accountRepository.findByIdWithLock(dto.getFromAccountId())
                    .orElseThrow(() -> {
                        log.error("출금계좌 락 획득 실패 - 계좌ID: {}", dto.getFromAccountId());
                        return new AccountNotFoundException("출금 계좌를 찾을 수 없습니다");
                    });
            toAccount = accountRepository.findByAccountNumberWithLock(toAccountNumber)
                    .orElseThrow(() -> {
                        log.error("입금계좌 락 획득 실패 - 계좌번호: {}", toAccountNumber);
                        return new AccountNotFoundException("입금 계좌를 찾을 수 없습니다");
                    });
        } else {
            toAccount = accountRepository.findByAccountNumberWithLock(toAccountNumber)
                    .orElseThrow(() -> {
                        log.error("입금계좌 락 획득 실패 - 계좌번호: {}", toAccountNumber);
                        return new AccountNotFoundException("입금 계좌를 찾을 수 없습니다");
                    });
            fromAccount = accountRepository.findByIdWithLock(dto.getFromAccountId())
                    .orElseThrow(() -> {
                        log.error("출금계좌 락 획득 실패 - 계좌ID: {}", dto.getFromAccountId());
                        return new AccountNotFoundException("출금 계좌를 찾을 수 없습니다");
                    });
        }
    }

    private void checkPassword(TransferRequestDTO dto) {
        if (dto.getType() == TransferType.INSTANT && !passwordEncoder.matches(dto.getPassword(), fromAccount.getPassword())) {
            log.error("이체 실패 - 비밀번호 불일치 (출금계좌: {})", dto.getFromAccountId());
            throw new InvalidAccountPasswordException("계좌 비밀번호가 일치하지 않습니다");
        }
    }

    private void executeTransfer(TransferRequestDTO dto) {
        log.info("이체 실행 - 출금계좌: {}, 입금계좌: {}, 금액: {}",
                fromAccount.getAccountNumber(), toAccount.getAccountNumber(), dto.getAmount());
        fromAccount.withdraw(dto.getAmount());
        toAccount.deposit(dto.getAmount());
    }

}
