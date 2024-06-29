package com.MovieMonster.demo.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CastMemberDto {
    private int id;
    private String name;
    private String character;
    private String profilePath;
}
