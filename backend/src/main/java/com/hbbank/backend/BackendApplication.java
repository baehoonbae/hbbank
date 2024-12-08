package com.hbbank.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.hbbank.backend.domain.AccountType;
import com.hbbank.backend.repository.AccountTypeRepository;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(AccountTypeRepository accountTypeRepository) {
		return args -> {
			if (accountTypeRepository.count() == 0) {
				accountTypeRepository.save(AccountType.builder()
					.code("HBFREE")
					.name("HB 프리체킹")
					.description("수수료 면제 자유입출금통장")
					.interestRate(0.1)
					.minimumBalance(0L)
					.build());

				accountTypeRepository.save(AccountType.builder()
					.code("HBSAVE")
					.name("HB 슈퍼정기예금")
					.description("높은 이자율의 정기예금")
					.interestRate(4.5)
					.minimumBalance(100000L)
					.build());

				accountTypeRepository.save(AccountType.builder()
					.code("HBYOUTH")
					.name("HB 영챌린지통장") 
					.description("만 34세 이하 전용 급여통장")
					.interestRate(2.8)
					.minimumBalance(0L)
					.build());

				accountTypeRepository.save(AccountType.builder()
					.code("HBPLUS")
					.name("HB 플러스통장")
					.description("입출금액에 따른 포인트 적립")
					.interestRate(1.2)
					.minimumBalance(50000L)
					.build());

				accountTypeRepository.save(AccountType.builder()
					.code("HBDIGITAL")
					.name("HB 디지털통장")
					.description("온라인 전용 특별우대통장")
					.interestRate(2.0)
					.minimumBalance(0L)
					.build());

				accountTypeRepository.save(AccountType.builder()
					.code("HBSENIOR")
					.name("HB 시니어플러스")
					.description("만 60세 이상 전용 연금통장")
					.interestRate(3.0)
					.minimumBalance(10000L)
					.build());

				accountTypeRepository.save(AccountType.builder()
					.code("HBBIZ")
					.name("HB 비즈니스통장")
					.description("사업자 전용 입출금통장")
					.interestRate(1.5)
					.minimumBalance(100000L)
					.build());
			}
		};
	}
}
