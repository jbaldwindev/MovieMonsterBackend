package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "friend_requests")
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sender_id", referencedColumnName = "id")
    private UserEntity sender;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="receiver_id", referencedColumnName = "id")
    private UserEntity receiver;
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;
    private LocalDateTime localDateTime;

}
