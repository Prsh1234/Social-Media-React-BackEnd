package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "friends", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "friend_id"})
})
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The main user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The friend user
    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    public Friend() {}

    public Friend(User user, User friend) {
        this.user = user;
        this.friend = friend;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }
}
