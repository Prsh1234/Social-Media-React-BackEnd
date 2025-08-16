package com.example.DTO;

public class FriendRequestDTO {
    private int senderId;
    private int receiverId;

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
}
