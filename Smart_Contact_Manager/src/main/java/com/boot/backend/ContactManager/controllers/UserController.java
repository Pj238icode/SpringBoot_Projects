package com.boot.backend.ContactManager.controllers;

import com.boot.backend.ContactManager.Responses.ApiResponse;
import com.boot.backend.ContactManager.Responses.ProfileResponse;
import com.boot.backend.ContactManager.Responses.profileDto;
import com.boot.backend.ContactManager.dtos.changePasswordDto;
import com.boot.backend.ContactManager.entities.Contact;
import com.boot.backend.ContactManager.entities.User;
import com.boot.backend.ContactManager.jwt.JwtHelper;
import com.boot.backend.ContactManager.repositories.ContactRepository;
import com.boot.backend.ContactManager.repositories.UserRespository;
import com.boot.backend.ContactManager.requests.updateProfileRequest;
import com.boot.backend.ContactManager.services.ImageService;
import com.boot.backend.ContactManager.services.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private Logger logger= LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;


    @Autowired
    private ImageService imageService;


    @GetMapping("/getTotalContacts")
    public ResponseEntity<?> getTotalContacts(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity
                        .status(401)
                        .body(new ApiResponse<>(false, "Unauthorized: No user details found", null));
            }

            String userEmail = userDetails.getUsername();
            User user = userRespository.findByEmail(userEmail);

            if (user == null) {
                return ResponseEntity
                        .status(404)
                        .body(new ApiResponse<>(false, "User not found", null));
            }

            long totalContacts = userService.getTotalContacts(user.getId());

            return ResponseEntity.ok(new ApiResponse<>(true, "Contacts Fetched", totalContacts));

        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null));
        }
    }

    @GetMapping("/favourite-contacts")
    public ResponseEntity<?> getFavouriteContactsCount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity
                        .status(401)
                        .body(new ApiResponse<>(false, "Unauthorized: No user details found", null));
            }

            String userEmail = userDetails.getUsername();
            User user = userRespository.findByEmail(userEmail);

            if (user == null) {
                return ResponseEntity
                        .status(404)
                        .body(new ApiResponse<>(false, "User not found", null));
            }

            long favouriteContacts = userService.getTotalFavouriteContacts(user.getId());

            return ResponseEntity.ok(new ApiResponse<>(true, "Favourite contacts count fetched", favouriteContacts));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body(new ApiResponse<>(false, "An unexpected error occurred: " + e.getMessage(), null));
        }
    }

    @GetMapping("/contacts-last-six-months-count")
    public ResponseEntity<?> getContactsCountLastSixMonths(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        User user = userRespository.findByEmail(userEmail);

        Map<Integer, Long> counts = userService.getContactsCountLastSixMonths(user.getId());

        return ResponseEntity.ok(new ApiResponse<>(true, "Contacts count per month fetched", counts));
    }

    @GetMapping("/getCredits")
    public ResponseEntity<?> getUserCredits(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 1. Get the logged-in user's email
            String userEmail = userDetails.getUsername();

            // 2. Fetch the user from the repository
            User user = userRespository.findByEmail(userEmail);
            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found", null));
            }

            // 3. Get the user's credits
            Long credits = user.getCredits();

            // 4. Return the response
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "User credits fetched", credits)
            );
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error fetching user credits", null));
        }
    }


    @PostMapping("/updateCredits")
    public ResponseEntity<?> updateCredits(
            @RequestParam("amount") Long amount, // Stripe sends in paise for INR
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 1. Find user
            User user = userRespository.findByEmail(userDetails.getUsername());

            // 2. Convert paise → rupees
            long amountInRupees = amount / 100;

            // 3. Credits = amount in rupees
            int creditsToAdd = (int) amountInRupees;

            // 4. Update user's credits
            user.setCredits(user.getCredits() + creditsToAdd);
            userRespository.save(user);

            // 5. Return response
            Map<String, Object> response = new HashMap<>();
            response.put("credits", user.getCredits());

            return new ResponseEntity<>(new ApiResponse<>(true, "Credits updated successfully!",response),HttpStatus.OK);

        } catch (Exception e) {

            return new ResponseEntity<>(new ApiResponse<>(false, "Credits updated successfully!",null),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody changePasswordDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(403)
                    .body(new ApiResponse<>(false, "Unauthorized: No authenticated user found", null));
        }

        String email = userDetails.getUsername();

        try {
            userService.changePassword(email, dto);
            return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully!", null));
        } catch (RuntimeException e) {
            // Handle known exceptions gracefully
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            // Handle unexpected exceptions
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "An unexpected error occurred", null));
        }
    }


    @GetMapping("/getProfile")
    public ResponseEntity<?> fetchProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
        }

        String username = userDetails.getUsername();
        User user = userRespository.findByEmail(username);

        if (user != null) {
            profileDto profileDto1 = modelMapper.map(user, profileDto.class);
            return ResponseEntity.ok(new ApiResponse<>(true, "User exists", profileDto1));
        } else {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "User does not exist", null));
        }
    }


    @PostMapping("/changeProfileImage")
    public ResponseEntity<?> changeProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile multipartFile) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
        }

        if (multipartFile == null || multipartFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "No file uploaded", null));
        }

        // ✅ Validate image format (png, jpg, jpeg only)
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || !isAllowedImageFormat(originalFilename)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Only PNG, JPG, and JPEG formats are allowed", null));
        }

        try {
            // 1️⃣ Get the logged-in user's record
            String email = userDetails.getUsername();
            User user = userRespository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found", null));
            }

            // 2️⃣ Upload the image using ImageService
            String imageUrl = imageService.uploadFile(multipartFile);

            // 3️⃣ Update user's profile image URL
            user.setPicture(imageUrl);
            userRespository.save(user);

            // 4️⃣ Return response
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile image updated successfully", imageUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update profile image: " + e.getMessage(), null));
        }
    }
    private boolean isAllowedImageFormat(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
    }


    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody updateProfileRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        ProfileResponse response = userService.updateUserProfile(email, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "User Updated Successfully", response));
    }














}
