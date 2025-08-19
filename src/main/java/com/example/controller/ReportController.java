package com.example.controller;

import com.example.DTO.UserPostDTO;
import com.example.model.Friend;
import com.example.model.Post;
import com.example.model.Report;
import com.example.model.User;
import com.example.repository.PostRepository;
import com.example.repository.ReportRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/post")
public class ReportController {

    @Autowired
    private PostRepository pRepo;

    @Autowired
    private UserRepository uRepo;

    @Autowired
    private ReportRepository reportRepo;

    @PostMapping("/report")
    public ResponseEntity<?> reportPost(
            @RequestParam int postId,
            @RequestParam int userId,
            @RequestParam(required = false) String reason) {

        Optional<Post> postOpt = pRepo.findById(postId);
        Optional<User> userOpt = uRepo.findById(userId);

        if (postOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Post not found"
            ));
        }
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        // Create and save a report
        Report report = new Report();
        report.setPost(postOpt.get());
        report.setReporter(userOpt.get());


        reportRepo.save(report);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post reported successfully"
        ));


    }

}
