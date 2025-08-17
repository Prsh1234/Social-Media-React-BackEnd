package com.example.repository;

import com.example.model.Comment;
import com.example.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
}
