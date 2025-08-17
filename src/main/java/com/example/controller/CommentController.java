package com.example.controller;

import com.example.DTO.CommentDTO;
import com.example.DTO.CommentRequest;
import com.example.model.Comment;
import com.example.model.Post;
import com.example.model.User;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "http://localhost:3000")
public class CommentController {

    @Autowired
    private CommentRepository cRepo;
    @Autowired
    private PostRepository pRepo;
    @Autowired
    private UserRepository uRepo;

    // ✅ Add comment
    @PostMapping("/addcomment")
    public ResponseEntity<?> addComment(@RequestBody CommentRequest req) {
        Post post = pRepo.findById(req.getPostId()).orElse(null);
        User user = uRepo.findById(req.getUserId()).orElse(null);

        if (post == null || user == null) {
            return ResponseEntity.badRequest().body("Invalid post or user");
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(req.getText());

        cRepo.save(comment);
        return ResponseEntity.ok("Comment added successfully");
    }


    // ✅ Get comments for a post
    @GetMapping("/getcomment")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@RequestParam int postId) {
        Post post = pRepo.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.badRequest().body(List.of());

        List<Comment> comments = cRepo.findByPostOrderByCreatedAtAsc(post);

        List<CommentDTO> dtos = comments.stream().map(c -> {
            CommentDTO dto = new CommentDTO();
            dto.setId(c.getId());
            dto.setUserId(c.getUser().getId());
            dto.setUserName(c.getUser().getUserName());
            dto.setContent(c.getContent());
            dto.setCreatedAt(c.getCreatedAt().toString());

            if (c.getUser().getProfilePic() != null) {
                dto.setProfilePic(Base64.getEncoder().encodeToString(c.getUser().getProfilePic()));
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ✅ Delete comment
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteComment(@RequestParam int commentId, @RequestParam int userId) {
        Comment comment = cRepo.findById(commentId).orElse(null);
        if (comment == null) {
            return ResponseEntity.badRequest().body("Comment not found");
        }

        if (comment.getUser().getId() != userId) {
            return ResponseEntity.status(403).body("You can only delete your own comments");
        }

        cRepo.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
