package com.boot.backend.ContactManager.controllers;

import com.boot.backend.ContactManager.Responses.ApiResponse;
import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.repositories.UserRespository;
import com.boot.backend.ContactManager.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final UserRespository userRepository;



    //Export Contacts
    @GetMapping("/export")
    public ResponseEntity<?> exportContacts(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername());
            if (user == null) {
                return new ResponseEntity<>(new ApiResponse<>(false,"Unauthorized User",null),HttpStatus.UNAUTHORIZED);
            }

            byte[] csvData = fileService.exportContacts(user);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvData);

        } catch (RuntimeException ex) {
            // Handle business logic errors like empty contacts, insufficient credits, etc.

            return new ResponseEntity<>(new ApiResponse<>(false,ex.getMessage(),null),HttpStatus.NOT_FOUND);


        } catch (Exception ex) {
            // Catch unexpected errors and return 500 with message
            return new ResponseEntity<>(new ApiResponse<>(false,ex.getMessage(),null),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Import Contacts
    @PostMapping("/import")
    public ResponseEntity<String> importContacts(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            // Call FileService to handle CSV or XLSX import
            fileService.importContacts(file, userDetails.getUsername());
            return ResponseEntity.ok("Contacts imported successfully");
        } catch (RuntimeException e) {
            // Handles user not found or insufficient credits
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to import contacts: " + e.getMessage());
        } catch (Exception e) {
            // Handles file parsing errors, IOExceptions, etc.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to import contacts: " + e.getMessage());
        }
    }


}
