package com.example.repository;

import com.example.model.Friend;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository  extends JpaRepository<Friend, Integer> {
    boolean existsByUserAndFriend(User sender, User receiver);
    List<Friend> findByUserIdOrFriendId(int userId, int friendId);

    void deleteByUserAndFriend(User user, User friend);

    void deleteByFriendAndUser(User friend, User user);

}
