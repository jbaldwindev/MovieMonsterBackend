package com.MovieMonster.demo.Repositories;

import com.MovieMonster.demo.Models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);

    @Query(value = "SELECT * FROM users WHERE similarity(username, :searchTerm) > 0.1 ORDER BY similarity(username, :searchTerm) DESC LIMIT 5", nativeQuery = true)
    List<UserEntity> findSimilarUsernames(@Param("searchTerm") String searchTerm);
}
