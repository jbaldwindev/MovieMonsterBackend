package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MovieComments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String movieComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="movie_id", referencedColumnName = "id")
    private Movie movie;
}
