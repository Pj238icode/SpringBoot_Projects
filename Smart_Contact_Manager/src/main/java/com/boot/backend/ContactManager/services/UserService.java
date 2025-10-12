package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.Responses.ApiResponse;
import com.boot.backend.ContactManager.Responses.ProfileResponse;
import com.boot.backend.ContactManager.dtos.UserDto;
import com.boot.backend.ContactManager.dtos.changePasswordDto;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.requests.updateProfileRequest;

import java.util.List;
import java.util.Map;

public interface UserService {
    public UserDto createUser(UserDto userDto);
    public long getTotalContacts(String userId);
    public long getTotalFavouriteContacts(String userId);
    public Map<Integer, Long> getContactsCountLastSixMonths(String userId);
    void changePassword(String email, changePasswordDto dto);
    public ProfileResponse updateUserProfile(String email, updateProfileRequest request);



}
