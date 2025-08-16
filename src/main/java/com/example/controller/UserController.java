package com.example.controller;

import com.example.DTO.UserDTO;
import com.example.model.User;
import com.example.repository.FriendRepository;
import com.example.repository.FriendRequestRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    @Autowired
    private UserRepository uRepo;
    @Autowired
    private FriendRepository fRepo;
    @Autowired
    private FriendRequestRepository frRepo;

    @GetMapping("/allusers")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam int currentUserId) {
        User currentUser = uRepo.findById(currentUserId).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(List.of());
        }
        List<User> allUsers = uRepo.findAll();
        List<UserDTO> userDTOs = allUsers.stream()
                // 1. Exclude current user
                .filter(user -> user.getId() != currentUserId)
                // 2. Exclude users who are already friends
                .filter(user -> !fRepo.existsByUserAndFriend(currentUser, user)
                        && !fRepo.existsByUserAndFriend(user, currentUser))
                // 3. Exclude users with pending friend requests (from either side)
                .filter(user -> !frRepo.existsBySenderAndReceiver(currentUser, user)
                        && !frRepo.existsBySenderAndReceiver(user, currentUser))
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setUserName(user.getUserName());
                    dto.setEmail(user.getEmail());
                    if (user.getProfilePic() != null) {
                        dto.setProfilePic(Base64.getEncoder().encodeToString(user.getProfilePic()));
                    }
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(userDTOs);
    }


    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadProfileOrCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,  // "profile" or "cover"
            @RequestParam("userId") int userId) {

        User user = uRepo.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            if (type.equalsIgnoreCase("profile")) {
                user.setProfilePic(file.getBytes());
            } else if (type.equalsIgnoreCase("cover")) {
                user.setCoverPhoto(file.getBytes());
            } else {
                return ResponseEntity.badRequest().body("Invalid type");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image bytes", e);
        }
            uRepo.save(user);
            return ResponseEntity.ok().body("Upload successful: " );
    }


    @GetMapping("/userdata")
    public ResponseEntity<UserDTO> getUserData(@RequestParam int currentUserId) {
        User user = uRepo.findById(currentUserId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(null);
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        if (user.getProfilePic() != null) {
            dto.setProfilePic(Base64.getEncoder().encodeToString(user.getProfilePic()));
        }
        if (user.getCoverPhoto() != null) {
            dto.setCoverPhoto(Base64.getEncoder().encodeToString(user.getCoverPhoto())) ;
        }
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(
            @RequestParam("currentUserId") int currentUserId,
            @RequestBody User updatedUser) {

        Optional<User> optionalUser = uRepo.findById(currentUserId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        // Only update the editable fields
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setUserName(updatedUser.getUserName());

        uRepo.save(user);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/changePassword")
    public ResponseEntity<?> changePassword(
            @RequestParam("currentUserId") int currentUserId,
            @RequestBody Map<String, String> passwords) {

        String currentPassword = passwords.get("currentPassword");
        String newPassword = passwords.get("newPassword");

        Optional<User> optionalUser = uRepo.findById(currentUserId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = optionalUser.get();

        // ✅ Verify current password
        if (!new BCryptPasswordEncoder().matches(currentPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Incorrect Password"));
        }
        // ✅ Update password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        user.setPassword(encoder.encode(newPassword));
        uRepo.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }
}