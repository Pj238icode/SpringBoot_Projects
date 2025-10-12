package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.Responses.ProfileResponse;
import com.boot.backend.ContactManager.dtos.UserDto;
import com.boot.backend.ContactManager.dtos.changePasswordDto;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.exceptions.UserException;
import com.boot.backend.ContactManager.repositories.ContactRepository;
import com.boot.backend.ContactManager.repositories.UserRespository;
import com.boot.backend.ContactManager.requests.updateProfileRequest;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ContactRepository contactRepository;


    @Override
    public UserDto createUser(UserDto userDto) {
        User user=modelMapper.map(userDto, User.class);
        User getUser=userRespository.findByEmail(user.getEmail());
        if(getUser==null){
            String id= UUID.randomUUID().toString();
            int min = 10;
            int max = 50;
            Random random = new Random();
            int randomNumber = random.nextInt(max - min + 1) + min;

            user.setId(id);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setUserId(randomNumber);
            User saveUser=userRespository.save(user);

            return modelMapper.map(saveUser,UserDto.class);

        }
        else{
            throw new UserException("User already exists");


        }


    }

    @Override
    public long getTotalContacts(String userId) {
        long total_contacts=contactRepository.countByUserId(userId);
        return total_contacts;



    }

    @Override
    public long getTotalFavouriteContacts(String userId) {
        // Use the repository method to count favourite contacts
        return contactRepository.countByUserIdAndFavouriteTrue(userId);
    }

    public Map<Integer, Long> getContactsCountLastSixMonths(String userId) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Object[]> results = contactRepository.countContactsPerMonth(userId, sixMonthsAgo);

        // Map month -> count
        Map<Integer, Long> counts = new LinkedHashMap<>();
        // initialize 0 for last 6 months
        for (int i = 5; i >= 0; i--) {
            int month = LocalDateTime.now().minusMonths(i).getMonthValue();
            counts.put(month, 0L);
        }

        // populate counts from query results
        for (Object[] row : results) {
            Integer month = ((Number) row[0]).intValue();
            Long total = ((Number) row[1]).longValue();
            counts.put(month, total);
        }

        return counts;
    }

    @Override
    public void changePassword(String email, changePasswordDto dto) {
        User user = userRespository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // ✅ Check old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // ✅ Check new password and confirm password
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // ✅ Prevent reuse of old password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the old password");
        }

        // ✅ Update password
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRespository.save(user);
    }




    @Transactional
    public ProfileResponse updateUserProfile(String email, updateProfileRequest request) {
        // ✅ Find user by email (username)
        User user = userRespository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // ✅ Update allowed fields
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber().trim());
        }

        // ✅ Save updates
        User savedUser = userRespository.save(user);

        // ✅ Return clean response DTO
        return modelMapper.map(savedUser, ProfileResponse.class);
    }


}
