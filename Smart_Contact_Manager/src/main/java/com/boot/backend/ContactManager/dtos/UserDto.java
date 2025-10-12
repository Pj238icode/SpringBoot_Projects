package com.boot.backend.ContactManager.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message="PhoneNumber is required")
    private String phoneNumber;
    @NotBlank(message="Password is required")
    @Size(min = 6,message = "Password must be atleast 6 characters in length")
    private String password;
}
