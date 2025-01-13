package com.MovieMonster.demo.Dto;

import lombok.Data;

import java.util.ArrayList;

@Data
public class SearchListDto {
    private ArrayList<FriendStatusDto> userConnections;
}
