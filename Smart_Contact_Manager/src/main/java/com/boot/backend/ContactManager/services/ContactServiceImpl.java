package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.dtos.ContactDto;
import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.repositories.ContactRepository;
import com.boot.backend.ContactManager.repositories.UserRespository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private UserRespository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ContactDto saveContact(ContactDto contactDto, String email) {
        try {
            // Map DTO → Entity
            Contact contact = modelMapper.map(contactDto, Contact.class);

            // Fetch user from DB by email
            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found with email: " + email);
            }

            // Check if user has enough credits
            if (user.getCredits() <= 0) {
                throw new RuntimeException("Insufficient credits to add a contact");
            }

            // Set user in the contact entity (this sets user_id in DB)
            contact.setUser(user);

            // Save contact
            Contact savedContact = contactRepository.save(contact);

            // Decrement user's credits by 1
            user.setCredits(user.getCredits() - 1);
            userRepository.save(user);

            // Map back to DTO
            return modelMapper.map(savedContact, ContactDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save contact: " + e.getMessage());
        }
    }





    @Override
    public List<ContactDto> getAllContacts() {
        return List.of();
    }

    @Override
    public ContactDto getContactById(Long id) {
        return null;
    }



    @Override
    public void deleteContact(Long id) {

    }
    @Override
    public Page<ContactDto> getContactsByUser(String username, Pageable pageable) {
        // Fetch contacts using repository method
        Page<Contact> contactsPage = contactRepository.findByUserEmail(username, pageable);

        // Map manually to ContactDto
        List<ContactDto> contactDtos = contactsPage.getContent().stream().map(contact -> {
            ContactDto dto = new ContactDto();
            dto.setId(contact.getId());
            dto.setFirstName(contact.getFirstName());
            dto.setLastName(contact.getLastName());
            dto.setEmail(contact.getEmail());
            dto.setPhone(contact.getPhone());
            dto.setAddress(contact.getAddress());
            dto.setCompany(contact.getCompany());
            dto.setJobTitle(contact.getJobTitle());
            dto.setWebsite(contact.getWebsite());
            dto.setDob(contact.getDob());
            dto.setFavourite(contact.isFavourite());
            dto.setImage1(contact.getImage());
            return dto;
        }).toList();

        // Return Page<ContactDto> with same pagination
        return new PageImpl<>(contactDtos, pageable, contactsPage.getTotalElements());
    }

    @Override
    public Page<ContactDto> searchContacts(String email, String field, String keyword, Pageable pageable) {
        Page<Contact> contactsPage;

        switch (field.toLowerCase()) {
            case "firstname" ->
                    contactsPage = contactRepository.findByUserEmailAndFirstNameContainingIgnoreCase(email, keyword, pageable);
            case "lastname" ->
                    contactsPage = contactRepository.findByUserEmailAndLastNameContainingIgnoreCase(email, keyword, pageable);
            case "email" ->
                    contactsPage = contactRepository.findByUserEmailAndEmailContainingIgnoreCase(email, keyword, pageable);
            case "phone" ->
                    contactsPage = contactRepository.findByUserEmailAndPhoneContainingIgnoreCase(email, keyword, pageable);
            default -> throw new IllegalArgumentException("Invalid search field: " + field);
        }

        // Convert to DTOs
        List<ContactDto> contactDtos = contactsPage.getContent().stream().map(contact -> {
            ContactDto dto = new ContactDto();
            dto.setFirstName(contact.getFirstName());
            dto.setLastName(contact.getLastName());
            dto.setEmail(contact.getEmail());
            dto.setPhone(contact.getPhone());
            dto.setAddress(contact.getAddress());
            dto.setCompany(contact.getCompany());
            dto.setJobTitle(contact.getJobTitle());
            dto.setWebsite(contact.getWebsite());
            dto.setDob(contact.getDob());
            dto.setFavourite(contact.isFavourite());
            dto.setImage1(contact.getImage());
            return dto;
        }).toList();

        return new PageImpl<>(contactDtos, pageable, contactsPage.getTotalElements());
    }

    @Override
    public void deleteContact(Long id, String email) {
        // Fetch contact
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));

        // Check ownership
        if (!contact.getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not authorized to delete this contact");
        }

        // Delete
        contactRepository.delete(contact);
    }

    @Transactional
    @Override

    public int mergeDuplicatesByEmail(User user) {
        int mergedCount = 0;

        List<String> duplicateEmails = contactRepository.findDuplicateEmailsByUser(user);

        for (String email : duplicateEmails) {
            List<Contact> duplicates = contactRepository.findByUserAndEmail(user, email);

            if (duplicates.size() > 1) {
                Contact primary = duplicates.get(0);

                for (int i = 1; i < duplicates.size(); i++) {
                    Contact dup = duplicates.get(i);

                    if (isEmpty(primary.getFirstName()) && !isEmpty(dup.getFirstName())) primary.setFirstName(dup.getFirstName());
                    if (isEmpty(primary.getLastName()) && !isEmpty(dup.getLastName())) primary.setLastName(dup.getLastName());
                    if (isEmpty(primary.getPhone()) && !isEmpty(dup.getPhone())) primary.setPhone(dup.getPhone());
                    if (isEmpty(primary.getAddress()) && !isEmpty(dup.getAddress())) primary.setAddress(dup.getAddress());
                    if (isEmpty(primary.getCompany()) && !isEmpty(dup.getCompany())) primary.setCompany(dup.getCompany());
                    if (isEmpty(primary.getJobTitle()) && !isEmpty(dup.getJobTitle())) primary.setJobTitle(dup.getJobTitle());
                    if (isEmpty(primary.getWebsite()) && !isEmpty(dup.getWebsite())) primary.setWebsite(dup.getWebsite());
                    if (isEmpty(primary.getDob()) && !isEmpty(dup.getDob())) primary.setDob(dup.getDob());
                    if (primary.getImage() == null && dup.getImage() != null) primary.setImage(dup.getImage());
                    if (!primary.isFavourite() && dup.isFavourite()) primary.setFavourite(true);

                    contactRepository.delete(dup);
                    mergedCount++;

                    // decrement user's credits per duplicate removed (optional)
                    user.setCredits(user.getCredits() - 1);
                }

                contactRepository.save(primary);
            }
        }

        userRepository.save(user); // save updated credits

        return mergedCount;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }



    @Override
    public Contact updateContact(Long id, ContactDto contactDto, String email) {
        // ✅ Ensure user exists
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // ✅ Fetch contact by ID
        Contact existingContact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));

        // ✅ Ensure ownership
        if (!existingContact.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to update this contact");
        }

        // ✅ Update fields
        existingContact.setFirstName(contactDto.getFirstName());
        existingContact.setLastName(contactDto.getLastName());
        existingContact.setEmail(contactDto.getEmail());
        existingContact.setPhone(contactDto.getPhone());
        existingContact.setAddress(contactDto.getAddress());
        existingContact.setCompany(contactDto.getCompany());
        existingContact.setJobTitle(contactDto.getJobTitle());
        existingContact.setWebsite(contactDto.getWebsite());
        existingContact.setDob(contactDto.getDob());
        existingContact.setFavourite(contactDto.isFavourite());

        if (contactDto.getImage1() != null) {
            existingContact.setImage(contactDto.getImage1()); // update image only if new one uploaded
        }

        // ✅ Save updated contact
        return contactRepository.save(existingContact);
    }



}
