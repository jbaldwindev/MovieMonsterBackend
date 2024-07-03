package com.MovieMonster.demo.Repositories;

import com.MovieMonster.demo.Models.MovieList;
import com.MovieMonster.demo.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieListRepository extends JpaRepository<MovieList, Integer> {
}
