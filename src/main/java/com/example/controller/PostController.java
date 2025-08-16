package com.example.controller;


import com.example.DTO.UserPostDTO;
import com.example.model.Friend;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.FriendRepository;
import com.example.repository.PostRepository;
import com.example.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/post")
@CrossOrigin(origins = "http://localhost:3000")
public class PostController {
    @Autowired
    private PostRepository pRepo;
    @Autowired
    private UserRepository uRepo; // to fetch User entity
    @Autowired
    private FriendRepository fRepo;

    @PostMapping(value = "/contentpost", consumes = {"multipart/form-data"})
    public ResponseEntity<String> postContent(@RequestParam("content") String content,
                                              @RequestParam("userId") int userId,
                                              @RequestParam(value = "image", required = false) MultipartFile image) {

        // Fetch the User entity using the userId
        User user = uRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = new Post();
        post.setContent(content);
        post.setUser(user);
        if(image!=null){
            try {
                post.setImage(image.getBytes()); // save bytes directly in DB

            } catch (IOException e) {
                throw new RuntimeException("Failed to read image bytes", e);
            }
        }

        pRepo.save(post);
        return ResponseEntity.ok("Post saved successfully!");
    }


    @GetMapping("/byuser")
    public ResponseEntity<List<UserPostDTO>> getPostsByUser(@RequestParam int posterId) {
        List<Post> posts = pRepo.findByUserId(posterId);

        List<UserPostDTO> postDTOs = posts.stream().map(post -> {
            UserPostDTO dto = new UserPostDTO();
            dto.setId(post.getId());
            dto.setContent(post.getContent());
            dto.setUserName(post.getUser().getUserName());
            dto.setPosterId(post.getUser().getId());

            if (post.getUser().getProfilePic() != null) {
                dto.setProfilePic(Base64.getEncoder().encodeToString(post.getUser().getProfilePic()));
            }

            if (post.getImage() != null) {
                dto.setImageBase64(Base64.getEncoder().encodeToString(post.getImage()));
            }
            return dto;
        }).toList();

        return ResponseEntity.ok(postDTOs);
    }

    @GetMapping("/timelineposts")
    public ResponseEntity<List<UserPostDTO>> getTimeline(@RequestParam int userId) {
        // Step 1: Get all friend relationships for this user
        List<Friend> friends = fRepo.findByUserIdOrFriendId(userId, userId);

        // Step 2: Build a set of all user IDs (the user + their friends)
        Set<Integer> userIds = new HashSet<>();
        userIds.add(userId); // include self
        for (Friend f : friends) {
            if (f.getUser().getId() == userId) {
                userIds.add(f.getFriend().getId());
            } else {
                userIds.add(f.getUser().getId());
            }
        }

        // Step 3: Get posts for all these user IDs
        List<Post> posts = pRepo.findByUserIdInOrderByCreatedAtDesc(userIds);

        // Step 4: Convert to DTO
        List<UserPostDTO> postDTOs = posts.stream().map(post -> {
            UserPostDTO dto = new UserPostDTO();
            dto.setId(post.getId());
            dto.setContent(post.getContent());
            dto.setUserName(post.getUser().getUserName());
            dto.setPosterId(post.getUser().getId());

            if (post.getUser().getProfilePic() != null) {
                dto.setProfilePic(Base64.getEncoder().encodeToString(post.getUser().getProfilePic()));
            }
            if (post.getImage() != null) {
                dto.setImageBase64(Base64.getEncoder().encodeToString(post.getImage()));
            }
            return dto;
        }).toList();

        return ResponseEntity.ok(postDTOs);
    }
    @DeleteMapping("/deletepost")
    @Transactional
    public ResponseEntity<?> deletePost(@RequestParam int postId, @RequestParam int userId) {
        // 1. Fetch the post
        Post post = pRepo.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Post not found"
            ));
        }

        // 2. Check if the user is the owner of the post
        if (post.getUser().getId() != userId) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "You are not authorized to delete this post"
            ));
        }

        // 3. Delete the post
        pRepo.delete(post);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Post deleted successfully"
        ));
    }


}
