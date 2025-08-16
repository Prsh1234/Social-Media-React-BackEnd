package com.example.repository;

import com.example.model.FriendRequest;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    List<FriendRequest> findByReceiver(User receiver);
    boolean existsBySenderAndReceiver(User sender, User receiver);
}
