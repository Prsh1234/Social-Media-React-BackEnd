package com.example.controller;


import com.example.DTO.ChatMessageDTO;
import com.example.model.ChatMessage;
import com.example.model.User;
import com.example.repository.ChatMessageRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:3000")
public class ChatController {
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/get/{user1}/{user2}")
    @ResponseBody
    public List<ChatMessageDTO> getHistory(@PathVariable int user1, @PathVariable int user2) {
        User u1 = userRepository.findById(user1).orElseThrow();
        User u2 = userRepository.findById(user2).orElseThrow();
        return chatMessageRepository
                .findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(u1, u2, u1, u2)
                .stream()
                .map(m -> new ChatMessageDTO(
                        m.getSender().getId(),
                        m.getReceiver().getId(),
                        m.getContent(),
                        m.getTimestamp()
                ))
                .toList();
    }
    @PostMapping("/send")
    public ChatMessageDTO sendMessage(@RequestBody ChatMessageDTO dto) {
        User sender = userRepository.findById(dto.getSenderId()).orElseThrow();
        User receiver = userRepository.findById(dto.getReceiverId()).orElseThrow();

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(dto.getContent());
        message.setTimestamp(LocalDateTime.now());

        chatMessageRepository.save(message);

        return new ChatMessageDTO(
                sender.getId(),
                receiver.getId(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}
