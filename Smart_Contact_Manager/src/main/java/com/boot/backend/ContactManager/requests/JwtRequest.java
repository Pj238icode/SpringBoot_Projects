package com.boot.backend.ContactManager.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtRequest {
    @NotBlank(message = "email is required")
    @Email(message="Invalid Email Format")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
}
