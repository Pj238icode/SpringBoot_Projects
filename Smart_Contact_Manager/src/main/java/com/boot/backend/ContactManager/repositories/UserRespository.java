package com.boot.backend.ContactManager.repositories;

import com.boot.backend.ContactManager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRespository extends JpaRepository<User, String> {
    User findByEmail(String email);


}
