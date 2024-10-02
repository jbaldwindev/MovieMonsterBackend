package com.MovieMonster.demo.Repositories;
import com.MovieMonster.demo.Models.Movie;
import com.MovieMonster.demo.Models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Optional<Movie> findByMovieId(int id);
}
