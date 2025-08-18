package com.example.repository;

import com.example.model.FriendRequest;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {

    // Get all requests received by a user
    List<FriendRequest> findByReceiver(User receiver);

    // Check if a request exists from sender to receiver
    boolean existsBySenderAndReceiver(User sender, User receiver);

    // Get a request from a specific sender to a specific receiver
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
}
