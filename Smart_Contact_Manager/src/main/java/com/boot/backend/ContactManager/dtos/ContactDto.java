package com.boot.backend.ContactManager.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDto {

    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(\\+?[0-9]{7,15})$",
            message = "Phone number must be valid (7–15 digits, optional +)"
    )
    private String phone;

    // 🔹 Change to MultipartFile (since you're uploading an image)

    private MultipartFile image;


    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String company;


    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;


    @Pattern(
            regexp = "^(https?:\\/\\/([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(\\/[a-zA-Z0-9-._~:/?#\\[\\]@!$&'()*+,;=]*)?)$",
            message = "Website must be a valid URL"
    )
    private String website;


    @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "DOB must be in the format YYYY-MM-DD"
    )
    private String dob;

    private boolean favourite;

    private String image1;


}
