package com.guruge.hardware.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestRespond {

    @NotBlank(message = "Status is required")
    private String status;

    private String adminResponse;
}
