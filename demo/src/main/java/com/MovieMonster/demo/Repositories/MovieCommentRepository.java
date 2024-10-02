package com.MovieMonster.demo.Repositories;

import com.MovieMonster.demo.Models.MovieComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieCommentRepository extends JpaRepository<MovieComment, Integer> {
}
