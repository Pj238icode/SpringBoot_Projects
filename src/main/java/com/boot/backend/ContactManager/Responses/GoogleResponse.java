package com.boot.backend.ContactManager.Responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleResponse {
    private String token;
    private String email;
    private String name;
    private String picture;
    private Long credits;

}
