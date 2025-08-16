package com.example.repository;

import com.example.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByUserId(int posterId);

    List<Post> findByUserIdInOrderByCreatedAtDesc(Collection<Integer> userIds);
}
