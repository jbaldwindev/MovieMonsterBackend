package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String username;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name="user_roles", joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    //TODO add one to one relationship with MovieList
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "list_id", referencedColumnName = "id")
    private MovieList movieList;

    @ManyToMany
    @JoinTable(name="user_friends",
            joinColumns = @JoinColumn(name="user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="friend_id", referencedColumnName = "id"))
    private List<UserEntity> friends = new ArrayList<>();
    @OneToMany(mappedBy = "sender")
    private List<FriendRequest> sentRequests = new ArrayList<>();
    @OneToMany(mappedBy = "receiver")
    private List<FriendRequest> receivedRequests = new ArrayList<>();
    private String icon;
}
