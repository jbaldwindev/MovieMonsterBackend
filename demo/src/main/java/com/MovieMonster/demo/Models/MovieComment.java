package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    @Column(length = 3000)
    private String movieComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="movie_id", referencedColumnName = "id")
    private Movie movie;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CommentLike> commentLikeList = new ArrayList<>();
}
