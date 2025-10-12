package com.boot.backend.ContactManager.repositories;

import com.boot.backend.ContactManager.dtos.ContactDto;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    Page<Contact> findByUserEmail(String email, Pageable pageable);

    List<Contact> findByUserEmail(String email);

    long countByUserId(String userId);

    long countByUserIdAndFavouriteTrue(String userId);


    @Query("""
    SELECT FUNCTION('MONTH', c.createdAt) AS month, COUNT(c) AS total
    FROM contacts c
    WHERE c.user.id = :userId AND c.createdAt >= :fromDate
    GROUP BY FUNCTION('MONTH', c.createdAt)
    ORDER BY month
""")
    List<Object[]> countContactsPerMonth(@Param("userId") String userId,
                                         @Param("fromDate") LocalDateTime fromDate);

    Page<Contact> findByUserEmailAndFirstNameContainingIgnoreCase(String email, String firstName, Pageable pageable);
    Page<Contact> findByUserEmailAndLastNameContainingIgnoreCase(String email, String lastName, Pageable pageable);
    Page<Contact> findByUserEmailAndEmailContainingIgnoreCase(String email, String emailParam, Pageable pageable);
    Page<Contact> findByUserEmailAndPhoneContainingIgnoreCase(String email, String phone, Pageable pageable);


    @Query("SELECT c.email FROM contacts c WHERE c.user = :user AND c.email IS NOT NULL AND c.email <> '' GROUP BY c.email HAVING COUNT(c.email) > 1")
    List<String> findDuplicateEmailsByUser(User user);


    List<Contact> findByUserAndEmail(User user, String email);

    List<Contact> findByDob(String dob);





}
