package com.MovieMonster.demo.Repositories;

import com.MovieMonster.demo.Models.MovieRating;
import com.MovieMonster.demo.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRatingRepository extends JpaRepository<MovieRating, Integer> {
}
