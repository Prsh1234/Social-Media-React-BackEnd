package com.example.repository;

import com.example.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report,Integer> {
    List<Report> findByPostId(int postId);
    boolean existsByPostId(int postId);  // very useful for filtering
}
