package com.MovieMonster.demo.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CommentLike")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="comment_id", referencedColumnName = "id")
    private MovieComment comment;
}
