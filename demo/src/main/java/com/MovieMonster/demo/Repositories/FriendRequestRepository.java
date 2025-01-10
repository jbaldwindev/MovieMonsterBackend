package com.MovieMonster.demo.Repositories;

import com.MovieMonster.demo.Models.FriendRequest;
import com.MovieMonster.demo.Models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    Boolean existsBySenderAndReceiver(UserEntity sender, UserEntity receiver);
    Optional<FriendRequest> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);
}
