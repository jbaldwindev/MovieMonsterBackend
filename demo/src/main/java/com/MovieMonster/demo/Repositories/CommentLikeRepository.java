package com.MovieMonster.demo.Repositories;

import com.MovieMonster.demo.Models.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Integer> {
}
