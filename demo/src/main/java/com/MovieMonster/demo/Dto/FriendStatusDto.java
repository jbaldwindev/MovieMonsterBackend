package com.MovieMonster.demo.Dto;

import lombok.Data;

@Data
public class FriendStatusDto {
    private Boolean isFriend;
    private Boolean requestPending;
    private String senderUsername;
    private String receiverUsername;
    private String requestedUsername;
    private String searchedUserIcon;
}
