package com.boot.backend.ContactManager.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@Entity(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class User {
    @Id
    private String id;
    private int userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String role;
    private String picture;
    private boolean enabled;
    private String provider;
    private Long Credits=0L;
    private int tokenVersion = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contact> contacts;


}
