package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "MovieRatings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieRating {
    @Id
    @GeneratedValue()
    private int id;
    private int movieId;
    private int rating;
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="list_id", referencedColumnName = "id")
    private MovieList movieList;
}
