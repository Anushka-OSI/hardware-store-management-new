package com.guruge.hardware.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String contactPerson;
    private String phone;

    @Email(message = "Invalid email")
    private String email;

    private String address;
    private String taxNumber;
    private String status;
    private String notes;
}
