package com.MovieMonster.demo.Repositories;

import com.MovieMonster.demo.Models.FriendRequest;
import com.MovieMonster.demo.Models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    Boolean existsBySenderAndReceiver(UserEntity sender, UserEntity receiver);
}
