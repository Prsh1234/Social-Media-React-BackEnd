package com.example.controller;


import com.example.DTO.UserPostDTO;
import com.example.model.Friend;
import com.example.model.Like;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/post")

public class PostController {
    @Autowired
    private PostRepository pRepo;
    @Autowired
    private UserRepository uRepo; // to fetch User entity
    @Autowired
    private FriendRepository fRepo;
    @Autowired
    private LikeRepository lRepo;
    @Autowired
    private ReportRepository rRepo;

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

        List<UserPostDTO> postDTOs = posts.stream()
                .filter(post -> !rRepo.existsByPostId(post.getId())) // exclude reported
                .map(post -> {
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
        List<Friend> friends = fRepo.findByUserIdOrFriendId(userId, userId);

        Set<Integer> userIds = new HashSet<>();
        userIds.add(userId);
        for (Friend f : friends) {
            if (f.getUser().getId() == userId) {
                userIds.add(f.getFriend().getId());
            } else {
                userIds.add(f.getUser().getId());
            }
        }

        List<Post> posts = pRepo.findByUserIdInOrderByCreatedAtDesc(userIds);

        List<UserPostDTO> postDTOs = posts.stream()
                .filter(post -> !rRepo.existsByPostId(post.getId())) // exclude reported
                .map(post -> {
                    UserPostDTO dto = new UserPostDTO();
                    dto.setId(post.getId());
                    dto.setContent(post.getContent());
                    dto.setUserName(post.getUser().getUserName());
                    dto.setPosterId(post.getUser().getId());
                    dto.setLikeCount(lRepo.countByPostId(post.getId()));
                    dto.setLiked(lRepo.findByUserIdAndPostId(userId, post.getId()).isPresent());

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
    @PostMapping("/like")
    public ResponseEntity<?> toggleLike(@RequestParam int postId, @RequestParam int userId) {
        Optional<Like> existing = lRepo.findByUserIdAndPostId(userId, postId);
        if (existing.isPresent()) {
            lRepo.delete(existing.get());
            return ResponseEntity.ok(Map.of(
                    "liked", false,
                    "likeCount", lRepo.countByPostId(postId)
            ));
        } else {
            Like like = new Like();
            like.setPost(pRepo.findById(postId).orElseThrow());
            like.setUser(uRepo.findById(userId).orElseThrow());
            lRepo.save(like);
            return ResponseEntity.ok(Map.of(
                    "liked", true,
                    "likeCount", lRepo.countByPostId(postId)
            ));
        }
    }


}
