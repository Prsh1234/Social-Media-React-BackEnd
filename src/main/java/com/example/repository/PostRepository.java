package com.example.repository;

import com.example.model.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    Page<Post> findByUserId(int userId, Pageable pageable);
    Page<Post> findByUserIdIn(Set<Integer> userIds, Pageable pageable);

    List<Post> findByUserIdInOrderByCreatedAtDesc(Collection<Integer> userIds);
}
