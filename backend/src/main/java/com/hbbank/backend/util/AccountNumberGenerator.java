package com.hbbank.backend.util;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {
    private static final String BANK_CODE = "793"; // HB은행 코드
    private static final Random random = new Random();

    public String generate(String accountTypeCode) {
        // 지점 코드 (001: 본점)
        String branchCode = "001";
        
        // 계좌 종류 코드 (2자리 숫자로 변환)
        String typeCode = convertTypeCodeToNumber(accountTypeCode);
        
        // 랜덤 일련번호 (6자리)
        String serialNumber = String.format("%06d", random.nextInt(1000000));
        
        // 기본 계좌번호 조합
        String baseNumber = BANK_CODE + branchCode + typeCode + serialNumber;
        
        // 검증번호 생성
        int checksum = generateChecksum(baseNumber);
        
        // 최종 계좌번호 포맷팅 (xxx-xxx-xx-xxxxxx-x)
        return String.format("%s-%s-%s-%s-%d", 
            baseNumber.substring(0, 3),
            baseNumber.substring(3, 6),
            baseNumber.substring(6, 8),
            baseNumber.substring(8, 14),
            checksum);
    }

    // 계좌 종류 코드를 2자리 숫자로 변환
    private String convertTypeCodeToNumber(String code) {
        switch (code) {
            case "HBFREE": return "01";
            case "HBSAVE": return "02";
            case "HBYOUTH": return "03";
            case "HBPLUS": return "04";
            case "HBDIGITAL": return "05";
            case "HBSENIOR": return "06";
            case "HBBIZ": return "07";
            default: return "00";
        }
    }

    // Luhn 알고리즘을 이용한 검증번호 생성
    private int generateChecksum(String number) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }
} 