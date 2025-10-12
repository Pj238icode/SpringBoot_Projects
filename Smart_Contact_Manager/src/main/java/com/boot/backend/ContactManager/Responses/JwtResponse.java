package com.boot.backend.ContactManager.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String name;
    private String email;
    private String token;
    private String picture;
    private Long Credits;
}
