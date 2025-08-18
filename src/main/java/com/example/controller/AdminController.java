package com.example.controller;

import com.example.DTO.UserDTO;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserRepository uRepo;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers(@RequestParam int adminId) {
        User currentUser = uRepo.findById(adminId).orElse(null);
        if (currentUser == null||currentUser.getRole().name().equals("USER")) {
            System.out.println(currentUser.getRole().name());
            return ResponseEntity.badRequest().body(List.of());
        }
        List<User> allUsers = uRepo.findAll();
        List<UserDTO> userDTOs = allUsers.stream()
                // 1. Exclude current user
                .filter(user -> user.getId() != adminId)
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setUserName(user.getUserName());
                    dto.setEmail(user.getEmail());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setRole(user.getRole().name());
                    if (user.getProfilePic() != null) {
                        dto.setProfilePic(Base64.getEncoder().encodeToString(user.getProfilePic()));
                    }
                    if (user.getCoverPhoto() != null) {
                        dto.setCoverPhoto(Base64.getEncoder().encodeToString(user.getCoverPhoto()));
                    }
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(userDTOs);
    }

    @DeleteMapping("/deleteuser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        User user = uRepo.findById(id).orElse(null);
        uRepo.delete(user);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
        ));
    }
}
