package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.dtos.ContactDto;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.repositories.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactPollingService {

    private final ContactRepository contactRepository;
    private final ModelMapper modelMapper;

    // 🧠 In-memory cache: userEmail → list of contact DTOs
    private final Map<String, List<ContactDto>> contactCache = new ConcurrentHashMap<>();

    /**
     * ✅ Scheduled polling task: runs every 10 seconds
     */
    @Scheduled(fixedRate = 10000)
    public void pollContactsFromDatabase() {
        try {
            log.debug("Polling contacts from database...");

            contactCache.clear();

            // 🔹 Fetch all contacts
            List<Contact> allContacts = contactRepository.findAll();

            // 🔸 Group contacts by user's email
            Map<String, List<Contact>> groupedByUser = allContacts.stream()
                    .collect(Collectors.groupingBy(contact -> contact.getUser().getEmail()));

            // 🔸 Convert to DTOs and update cache
            groupedByUser.forEach((email, contacts) -> {
                List<ContactDto> dtos = contacts.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                contactCache.put(email, dtos);
            });

            log.debug("✅ Contact cache updated for {} users", contactCache.size());

        } catch (Exception ex) {
            log.error("❌ Error while polling contacts: {}", ex.getMessage(), ex);
        }
    }

    /**
     * ✅ Returns cached contacts for the given user (email)
     */
    public List<ContactDto> getCachedContactsForUser(String email) {
        return contactCache.getOrDefault(email, Collections.emptyList());
    }

    /**
     * ✅ Convert Contact → ContactDto using ModelMapper
     */
    private ContactDto convertToDto(Contact contact) {
        ContactDto dto = modelMapper.map(contact, ContactDto.class);

        // Set the URL for frontend display
        if (contact.getImage() != null && !contact.getImage().isEmpty()) {
            dto.setImage1(contact.getImage()); // or the correct path
        } else {
            dto.setImage1(null);
        }

        return dto;
    }


}
