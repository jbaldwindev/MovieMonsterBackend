package com.MovieMonster.demo.Dto;

import com.MovieMonster.demo.Models.FriendRequest;
import lombok.Data;

import java.util.ArrayList;

@Data
public class RequestListDto {
    private ArrayList<FriendRequestDto> friendRequestList;
}
