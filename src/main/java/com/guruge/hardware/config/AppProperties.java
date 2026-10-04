package com.guruge.hardware.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Storage storage = new Storage();
    private Pagination pagination = new Pagination();
    private Numbering numbering = new Numbering();
    private Business business = new Business();

    @Data
    public static class Jwt {
        /** Base64 or plain secret (min 32 chars / 256 bits recommended). */
        private String secret = "change-me-please-use-a-very-long-secret-key-256bits-minimum!";
        /** Access token validity in milliseconds (default 24h). */
        private long expiration = 86400000L;
        /** Refresh token validity in milliseconds (default 7 days). */
        private long refreshExpiration = 604800000L;
    }

    @Data
    public static class Storage {
        private String location = "./uploads";
        private String productImages = "./uploads/product-images";
        /** Max file size in bytes (default 5MB). */
        private long maxFileSize = 5 * 1024 * 1024L;
        private List<String> allowedTypes = List.of(
                "image/jpeg", "image/png", "image/gif", "image/webp");
    }

    @Data
    public static class Pagination {
        private int defaultPageSize = 20;
        private int maxPageSize = 100;
    }

    @Data
    public static class Numbering {
        private String receiptPrefix = "INV";
        private String poPrefix = "PO";
        private String grPrefix = "GRN";
        private String returnPrefix = "RET";
        private String requestPrefix = "GRG-REQ";
    }

    @Data
    public static class Business {
        /** Maximum discount percent allowed per line / sale. */
        private double maxDiscountPercent = 30.0;
        /** Low-stock threshold fallback when product has none. */
        private int lowStockThreshold = 10;
        private String currency = "LKR";
        private String storeName = "Guruge Hardware";
    }
}
