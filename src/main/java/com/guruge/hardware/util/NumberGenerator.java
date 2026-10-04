package com.guruge.hardware.util;

import com.guruge.hardware.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class NumberGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final SecureRandom secureRandom = new SecureRandom();
    private final AppProperties appProperties;

    private String dated(String prefix, int randomDigits) {
        String date = LocalDate.now().format(DATE_FMT);
        int bound = (int) Math.pow(10, randomDigits);
        int num = secureRandom.nextInt(bound);
        String suffix = String.format("%0" + randomDigits + "d", num);
        return prefix + "-" + date + "-" + suffix;
    }

    /** INV-yyyyMMdd-###### */
    public String generateReceiptNumber() {
        return dated(appProperties.getNumbering().getReceiptPrefix(), 6);
    }

    public String generatePoNumber() {
        return dated(appProperties.getNumbering().getPoPrefix(), 6);
    }

    public String generateGrNumber() {
        return dated(appProperties.getNumbering().getGrPrefix(), 6);
    }

    public String generateReturnNumber() {
        return dated(appProperties.getNumbering().getReturnPrefix(), 6);
    }

    /** GRG-REQ-yyyyMMdd-#### */
    public String generateRequestId() {
        return dated(appProperties.getNumbering().getRequestPrefix(), 4);
    }
}
