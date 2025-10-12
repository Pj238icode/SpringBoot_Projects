package com.boot.backend.ContactManager.authentication;

import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.repositories.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRespository userRespository;




    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch the user by email from the database
        User user = userRespository.findByEmail(email);

        // If user not found, throw exception
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // Wrap the User entity into CustomUserDetails and return
        return new CustomUserDetails(user);
    }
}
