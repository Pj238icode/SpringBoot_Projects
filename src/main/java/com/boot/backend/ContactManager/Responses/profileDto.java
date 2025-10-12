package com.boot.backend.ContactManager.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class profileDto {
    private String id;
    private int userId; 
    private String name;
    private String email;
    private String phoneNumber;
    private String picture;
    private String role;
    private boolean enabled;
}
