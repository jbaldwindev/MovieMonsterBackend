package com.MovieMonster.demo.Dto;

import lombok.Data;

@Data
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer ";

    public AuthResponseDto(String accessToken, String refreshToken) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
