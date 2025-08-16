package com.example.controller;

import com.example.DTO.FriendRequestDTO;
import com.example.DTO.FriendRequestListDTO;
import com.example.DTO.UserDTO;
import com.example.model.Friend;
import com.example.model.FriendRequest;
import com.example.model.User;
import com.example.repository.FriendRepository;
import com.example.repository.FriendRequestRepository;
import com.example.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/friend")
@CrossOrigin(origins = "http://localhost:3000") // adjust if needed
public class FriendController {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @PostMapping("/postrequest")
    public ResponseEntity<?> sendFriendRequest(@RequestBody FriendRequestDTO request) {
        int senderId = request.getSenderId();
        int receiverId = request.getReceiverId();
        System.out.println(senderId+""+receiverId);
        if (senderId == receiverId) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "You cannot send a friend request to yourself")
            );
        }

        User sender = userRepository.findById(senderId).orElse(null);
        User receiver = userRepository.findById(receiverId).orElse(null);

        if (sender == null || receiver == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Invalid sender or receiver ID")
            );
        }
        boolean exists = friendRequestRepository.existsBySenderAndReceiver(sender, receiver);
        if (exists) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Friend request already sent")
            );
        }

        boolean alreadyFriends = friendRepository.existsByUserAndFriend(sender, receiver) ||
                friendRepository.existsByUserAndFriend(receiver, sender);
        if (alreadyFriends) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse(false, "Users are already friends")
            );
        }


        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setReceiver(receiver);
        friendRequest.setSender(sender);
        friendRequestRepository.save(friendRequest);

        return ResponseEntity.ok(new ApiResponse(true, "Friend request sent successfully"));
    }


    @GetMapping("/getrequests")
    public ResponseEntity<Map<String, Object>> getFriendRequestsByUserId(@RequestParam int userId) {
        User receiver = userRepository.findById(userId).orElse(null);
        if (receiver == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "data", List.of(),
                    "error", "User not found"
            ));
        }

        List<FriendRequestListDTO> senderList = friendRequestRepository.findByReceiver(receiver)
                .stream()
                .map(req -> {
                    User sender = req.getSender();
                    FriendRequestListDTO dto = new FriendRequestListDTO();
                    dto.setRequestId(req.getId());
                    dto.setUserName(sender.getUserName());
                    if (sender.getProfilePic() != null) {
                        dto.setProfilePic(Base64.getEncoder().encodeToString(sender.getProfilePic()));
                    }
                    return dto;
                }).toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", senderList
        ));
    }

    @PostMapping("/acceptrequest")
    public ResponseEntity<?> acceptFriendRequest(@RequestParam int requestId) {
        System.out.println(requestId);
        // 1. Find the friend request
        FriendRequest friendRequest = friendRequestRepository.findById(requestId).orElse(null);
        if (friendRequest == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Friend request not found"));
        }

        User sender = friendRequest.getSender();
        User receiver = friendRequest.getReceiver();

        // 2. Check if friendship already exists in either direction
        boolean alreadyFriends = friendRepository.existsByUserAndFriend(sender, receiver) ||
                friendRepository.existsByUserAndFriend(receiver, sender);
        if (alreadyFriends) {
            friendRequestRepository.delete(friendRequest); // cleanup
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Users are already friends"));
        }

        // 3. Create bidirectional friendship
        Friend friendship1 = new Friend();
        friendship1.setUser(sender);
        friendship1.setFriend(receiver);
        friendRepository.save(friendship1);

        Friend friendship2 = new Friend();
        friendship2.setUser(receiver);
        friendship2.setFriend(sender);
        friendRepository.save(friendship2);

        // 4. Delete the friend request
        friendRequestRepository.delete(friendRequest);

        return ResponseEntity.ok(new ApiResponse(true, "Friend request accepted"));
    }


    @PostMapping("/rejectrequest")
    public ResponseEntity<?> rejectFriendRequest(@RequestParam int requestId) {
        // 1. Find the friend request
        FriendRequest friendRequest = friendRequestRepository.findById(requestId).orElse(null);
        if (friendRequest == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Friend request not found"));
        }

        // 2. Delete the friend request
        friendRequestRepository.delete(friendRequest);

        return ResponseEntity.ok(new ApiResponse(true, "Friend request rejected"));
    }


    @GetMapping("/getfriends")
    public ResponseEntity<Map<String, Object>> getFriendsByUserId(@RequestParam int userId) {
        User currentUser = userRepository.findById(userId).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "data", List.of(),
                    "error", "User not found"
            ));
        }

        List<Friend> friends = friendRepository.findByUserIdOrFriendId(userId, userId);

        // Use a Set to prevent duplicates
        Set<Integer> seenIds = new HashSet<>();

        List<UserDTO> friendList = friends.stream()
                .map(friend -> {
                    User otherUser = (friend.getUser().getId() == userId)
                            ? friend.getFriend()
                            : friend.getUser();

                    // Skip if already added
                    if (!seenIds.add(otherUser.getId())) {
                        return null;
                    }

                    UserDTO dto = new UserDTO();
                    dto.setId(otherUser.getId());
                    dto.setUserName(otherUser.getUserName());
                    dto.setEmail(otherUser.getEmail());
                    dto.setFirstName(otherUser.getFirstName());
                    dto.setLastName(otherUser.getLastName());

                    if (otherUser.getProfilePic() != null) {
                        dto.setProfilePic(Base64.getEncoder().encodeToString(otherUser.getProfilePic()));
                    }
                    if (otherUser.getCoverPhoto() != null) {
                        dto.setCoverPhoto(Base64.getEncoder().encodeToString(otherUser.getCoverPhoto()));
                    }

                    return dto;
                })
                .filter(Objects::nonNull) // remove skipped duplicates
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", friendList
        ));
    }
    @PostMapping("/unfriend")
    @Transactional
    public ResponseEntity<?> unfriend(@RequestParam int friendId, @RequestParam int userId) {
        User currentUser = userRepository.findById(userId).orElse(null);
        User friendUser = userRepository.findById(friendId).orElse(null);
        if (currentUser == null || friendUser == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "User not found")
            );
        }

        // Delete friendship in both possible directions
        friendRepository.deleteByUserAndFriend(currentUser, friendUser);
        friendRepository.deleteByUserAndFriend(friendUser, currentUser);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Unfriended successfully"
        ));
    }
    // Inner class for uniform responses
    static class ApiResponse {
        private boolean success;
        private String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}