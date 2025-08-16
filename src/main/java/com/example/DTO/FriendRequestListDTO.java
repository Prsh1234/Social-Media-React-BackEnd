package com.example.DTO;

public class FriendRequestListDTO {
    private Long requestId;
    private String userName;
    private String profilePic; // optional Base64 image

    private int mutual;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }
    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public int getMutual() {
        return mutual;
    }

    public void setMutual(int mutual) {
        this.mutual = mutual;
    }
}
