package com.MovieMonster.demo.Dto;

import com.MovieMonster.demo.Models.RequestStatus;
import com.MovieMonster.demo.Models.UserEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestDto {
    private int id;
    private String sender;
    private String receiver;
    private RequestStatus requestStatus;
    private LocalDateTime localDateTime;
}
