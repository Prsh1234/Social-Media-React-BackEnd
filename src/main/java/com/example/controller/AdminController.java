package com.example.controller;

import com.example.DTO.ReportDTO;
import com.example.DTO.UserDTO;
import com.example.DTO.UserPostDTO;
import com.example.model.Post;
import com.example.model.Report;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.PostRepository;
import com.example.repository.ReportRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private PostRepository pRepo;

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

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        // Prevent deleting admins
        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "Admin users cannot be deleted"
            ));
        }

        uRepo.delete(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
        ));
    }

    @DeleteMapping("/deletepost/{id}")
    public ResponseEntity<?> deletePost(@PathVariable int id) {
        Post post = pRepo.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Post not found"
            ));
        }

        pRepo.delete(post);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post deleted successfully"
        ));
    }
    @DeleteMapping("/deletereport/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable int id) {
        Report report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Report not found"
            ));
        }

        reportRepository.delete(report);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Report deleted successfully"
        ));
    }
    @GetMapping("/allreports")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        List<Report> reports = reportRepository.findAll();

        List<ReportDTO> reportDTOs = reports.stream().map(r -> {
            ReportDTO dto = new ReportDTO();
            dto.setId(r.getId());
            dto.setReporterId(r.getReporter().getId());
            dto.setReporterName(r.getReporter().getUserName());
            dto.setCreatedAt(r.getCreatedAt());

            // build UserPostDTO from Post
            Post post = r.getPost();
            UserPostDTO postDTO = new UserPostDTO();
            postDTO.setId(post.getId());
            postDTO.setPosterId(post.getUser().getId());
            postDTO.setUserName(post.getUser().getUserName());
            postDTO.setContent(post.getContent());

            if (post.getUser().getProfilePic() != null) {
                postDTO.setProfilePic(Base64.getEncoder().encodeToString(post.getUser().getProfilePic()));
            }
            if (post.getImage() != null) {
                postDTO.setImageBase64(Base64.getEncoder().encodeToString(post.getImage()));
            }

            dto.setUserPostDTO(postDTO);

            return dto;
        }).toList();

        return ResponseEntity.ok(reportDTOs);
    }

}
