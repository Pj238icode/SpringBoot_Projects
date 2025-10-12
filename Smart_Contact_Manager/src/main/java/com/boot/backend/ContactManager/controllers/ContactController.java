package com.boot.backend.ContactManager.controllers;

import com.boot.backend.ContactManager.Responses.ApiResponse;
import com.boot.backend.ContactManager.Responses.ContactResponse;
import com.boot.backend.ContactManager.dtos.ContactDto;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.jwt.JwtHelper;
import com.boot.backend.ContactManager.repositories.ContactRepository;
import com.boot.backend.ContactManager.repositories.UserRespository;
import com.boot.backend.ContactManager.services.ContactPollingService;
import com.boot.backend.ContactManager.services.ContactService;
import com.boot.backend.ContactManager.services.ImageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;




@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private Logger logger= LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;
    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private ContactRepository  contactRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserRespository userRepository;

    @Autowired
    private ContactPollingService contactPollingService;





    @PostMapping("/saveContact")
    public ResponseEntity<?> saveContact(
            @Valid @ModelAttribute ContactDto contactDto,
            BindingResult result,                       // MUST be immediately after @Valid param
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader("Authorization") String authHeader) {

        // Validate image only if uploaded
        if (image != null && !image.isEmpty()) {
            String fileName = image.getOriginalFilename();
            if (fileName != null) {
                String lowerCaseName = fileName.toLowerCase();
                if (!(lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg"))) {
                    result.rejectValue("image", "InvalidFormat", "Only PNG, JPG, and JPEG image types are allowed");
                }
            }
        }

        // If there are validation errors (from @Valid or image), collect and return
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            logger.info("Validation errors: {}", errors);
            return new ResponseEntity<>(new ApiResponse<>(false, "Please fix the errors", errors), HttpStatus.BAD_REQUEST);
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(new ApiResponse<>(false, "Missing or invalid Authorization header", null),
                    HttpStatus.FORBIDDEN);
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtHelper.extractUsername(token);

            // Upload the image only if provided
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageService.uploadFile(image);
                contactDto.setImage1(imageUrl);
            }

            ContactDto saved = contactService.saveContact(contactDto, email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Contact saved successfully", saved));

        } catch (Exception e) {
            logger.error("Error saving contact", e);
            return new ResponseEntity<>(new ApiResponse<>(false, e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/all")
    public ResponseEntity<ContactResponse> getAllContacts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        String usernameOrEmail = userDetails.getUsername();  // can be email if that's what you're using

        // 1️⃣ Try fetching from the backend polling cache
        List<ContactDto> cachedContacts = contactPollingService.getCachedContactsForUser(usernameOrEmail);

        if (!cachedContacts.isEmpty()) {
            // Sort manually to match request params
            cachedContacts.sort((c1, c2) -> {
                int compareVal;
                switch (sortBy) {
                    case "lastName":
                        compareVal = c1.getLastName().compareToIgnoreCase(c2.getLastName());
                        break;
                    case "email":
                        compareVal = c1.getEmail().compareToIgnoreCase(c2.getEmail());
                        break;
                    case "phone":
                        compareVal = c1.getPhone().compareToIgnoreCase(c2.getPhone());
                        break;
                    default:
                        compareVal = c1.getFirstName().compareToIgnoreCase(c2.getFirstName());
                }
                return order.equalsIgnoreCase("desc") ? -compareVal : compareVal;
            });

            // Apply pagination manually
            int start = page * size;
            int end = Math.min(start + size, cachedContacts.size());
            List<ContactDto> paged = start < end ? cachedContacts.subList(start, end) : List.of();

            ContactResponse response = new ContactResponse(
                    paged,
                    page,
                    (int) Math.ceil((double) cachedContacts.size() / size),
                    cachedContacts.size(),
                    size
            );

            return ResponseEntity.ok(response);
        }

        // 2️⃣ Fallback to DB if cache is empty (first load or polling not yet done)
        Sort sort = order.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContactDto> contactsPage = contactService.getContactsByUser(usernameOrEmail, pageable);

        ContactResponse response = new ContactResponse(
                contactsPage.getContent(),
                contactsPage.getNumber(),
                contactsPage.getTotalPages(),
                contactsPage.getTotalElements(),
                contactsPage.getSize()
        );
        logger.info(response.toString());



        return ResponseEntity.ok(response);
    }




    @PutMapping("update/{id}")
    public ResponseEntity<ApiResponse<ContactDto>> updateContact(
            @PathVariable Long id,
            @ModelAttribute ContactDto contactDto,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            // ✅ Check authentication first
            if (userDetails == null || userDetails.getUsername() == null) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse<>(false, "Unauthorized: No authenticated user found", null));
            }

            String email = userDetails.getUsername(); // Extract logged-in user's email/username

            // ✅ If image is uploaded, save to S3 and update image URL
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = imageService.uploadFile(imageFile);
                contactDto.setImage1(imageUrl);
            }

            // ✅ Update contact and get back entity
            Contact updatedContact = contactService.updateContact(id, contactDto, email);

            // ✅ Convert entity -> DTO
            ContactDto updatedDto = new ContactDto();
            updatedDto.setId(updatedContact.getId());
            updatedDto.setFirstName(updatedContact.getFirstName());
            updatedDto.setLastName(updatedContact.getLastName());
            updatedDto.setEmail(updatedContact.getEmail());
            updatedDto.setPhone(updatedContact.getPhone());
            updatedDto.setAddress(updatedContact.getAddress());
            updatedDto.setCompany(updatedContact.getCompany());
            updatedDto.setJobTitle(updatedContact.getJobTitle());
            updatedDto.setWebsite(updatedContact.getWebsite());
            updatedDto.setDob(updatedContact.getDob());
            updatedDto.setFavourite(updatedContact.isFavourite());
            updatedDto.setImage1(updatedContact.getImage()); // ✅ map entity image -> DTO image1

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Contact updated successfully!", updatedDto)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "Failed to update contact: " + e.getMessage(), null)
            );
        }
    }




    @GetMapping("/search")
    public ResponseEntity<ContactResponse> searchContacts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String field,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        String username = userDetails.getUsername();

        Page<ContactDto> contactsPage = contactService.searchContacts(username, field, keyword, pageable);

        ContactResponse response = new ContactResponse(
                contactsPage.getContent(),
                contactsPage.getNumber(),
                contactsPage.getTotalPages(),
                contactsPage.getTotalElements(),
                contactsPage.getSize()
        );

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id, Authentication authentication) {
        try {
            // Extract logged-in user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername(); // in your case, username = email

            contactService.deleteContact(id, email);

            ApiResponse<String> response = new ApiResponse<>(
                    true,
                    "Contact deleted successfully",
                    null
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/merge-duplicates")
    public ResponseEntity<?> mergeDuplicates(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Unauthorized user", null));
        }

        try {
            int mergedCount = contactService.mergeDuplicatesByEmail(user);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Merged " + mergedCount + " duplicate contacts", mergedCount)
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }
    }











}
