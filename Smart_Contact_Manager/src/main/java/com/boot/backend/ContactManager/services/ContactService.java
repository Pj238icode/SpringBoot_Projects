package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.dtos.ContactDto;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContactService {
    ContactDto saveContact(ContactDto contactDto,String userId);
    List<ContactDto> getAllContacts();
    ContactDto getContactById(Long id);
    Contact updateContact(Long id, ContactDto contactDto,String email);
    void deleteContact(Long id);
     Page<ContactDto> getContactsByUser(String username, Pageable pageable);

    Page<ContactDto> searchContacts(String email, String field, String keyword, Pageable pageable);

    void deleteContact(Long id, String email);

    public int mergeDuplicatesByEmail(User user);


}
