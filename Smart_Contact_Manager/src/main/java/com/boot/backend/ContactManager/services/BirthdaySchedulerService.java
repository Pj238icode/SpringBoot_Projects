package com.boot.backend.ContactManager.services;

import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.repositories.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BirthdaySchedulerService {

    private final ContactRepository contactRepository;
    private final EmailService emailService;


    @Scheduled(cron = "0 0 9 * * *")

    public void sendDailyBirthdayEmails() {

        String today = LocalDate.now().toString();
        List<Contact> birthdayContacts = contactRepository.findByDob(today);

        for (Contact contact : birthdayContacts) {
            if (contact.getUser() != null) {
                emailService.sendBirthdayEmail(contact.getUser(), contact);
            }
        }


    }
}
