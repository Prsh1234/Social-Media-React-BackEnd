package com.example.repository;

import com.example.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByUserIdAndPostId(int userId, int postId);
    int countByPostId(int postId);
    List<Like> findByPostId(int postId);
}
