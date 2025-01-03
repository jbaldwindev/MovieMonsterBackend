package com.MovieMonster.demo.Dto;

import lombok.Data;

@Data
public class SendRequestDto {
    private String senderUsername;
    private String receiverUsername;
}
