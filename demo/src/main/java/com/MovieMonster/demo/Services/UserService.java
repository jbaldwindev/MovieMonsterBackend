package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Dto.FriendListDto;
import com.MovieMonster.demo.Dto.FriendRequestDto;
import com.MovieMonster.demo.Dto.RequestListDto;
import com.MovieMonster.demo.Dto.RequestResponseDto;
import com.MovieMonster.demo.Models.FriendRequest;
import com.MovieMonster.demo.Models.RequestStatus;
import com.MovieMonster.demo.Models.UserEntity;
import com.MovieMonster.demo.Repositories.FriendRequestRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    public void addFriend(String username, String friendUsername) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        Optional<UserEntity> retrievedFriend = userRepository.findByUsername(friendUsername);
        if (retrievedUser.isPresent() && retrievedFriend.isPresent()) {
            UserEntity user = retrievedUser.get();
            UserEntity friend = retrievedFriend.get();
            user.getFriends().add(friend);
            friend.getFriends().add(user);
            userRepository.save(user);
            userRepository.save(friend);
        } else {
            throw new IllegalArgumentException("Sender or receiver does not exist");
        }
    }

    //TODO implement removeFriend function

    public void sendRequest(String senderUsername, String receiverUsername) {
        UserEntity sender;
        UserEntity receiver;
        Optional<UserEntity> retrievedSender = userRepository.findByUsername(senderUsername);
        Optional<UserEntity> retrievedReceiver = userRepository.findByUsername(receiverUsername);
        if (retrievedSender.isPresent() && retrievedReceiver.isPresent()) {
            sender = retrievedSender.get();
            receiver = retrievedReceiver.get();
        } else {
            throw new NoSuchElementException("Sender or Receiver is not present");
        }
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new IllegalArgumentException("Friend request between these users already exists");
        }
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setRequestStatus(RequestStatus.PENDING);
        friendRequest.setReceiver(receiver);
        friendRequest.setSender(sender);
        friendRequest.setLocalDateTime(LocalDateTime.now());
        friendRequestRepository.save(friendRequest);
        sender.getSentRequests().add(friendRequest);
        receiver.getReceivedRequests().add(friendRequest);
        userRepository.save(sender);
        userRepository.save(receiver);
    }

    public void handleRequestResponse(RequestResponseDto requestResponseDto) {
        System.out.println("request id: " + requestResponseDto.getRequestId());
        System.out.println("request is accepted: " + requestResponseDto.getIsAccepted());
        Optional<FriendRequest> retrievedRequest = friendRequestRepository.findById(requestResponseDto.getRequestId());
        if (retrievedRequest.isPresent()) {
            FriendRequest friendRequest = retrievedRequest.get();
            UserEntity sender = friendRequest.getSender();
            UserEntity receiver = friendRequest.getReceiver();
            if (requestResponseDto.getIsAccepted()) {
                System.out.println("The thing is making it in this if statement");
                addFriend(sender.getUsername(), receiver.getUsername());
            }
            sender.getSentRequests().remove(friendRequest);
            receiver.getReceivedRequests().remove(friendRequest);
            userRepository.save(sender);
            userRepository.save(receiver);
            friendRequestRepository.delete(friendRequest);
        } else {
            throw new IllegalArgumentException("Friend request does not exist");
        }
    }

    public RequestListDto getReceivedRequests(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            RequestListDto requestListDto = new RequestListDto();
            ArrayList<FriendRequestDto> friendRequestList = new ArrayList<>();
            for (FriendRequest friendRequest : user.getReceivedRequests()) {
                FriendRequestDto friendRequestDto = new FriendRequestDto();
                friendRequestDto.setId(friendRequest.getId());
                friendRequestDto.setReceiver(friendRequest.getReceiver().getUsername());
                friendRequestDto.setSender(friendRequest.getSender().getUsername());
                friendRequestDto.setRequestStatus(friendRequest.getRequestStatus());
                friendRequestDto.setLocalDateTime(friendRequest.getLocalDateTime());
                friendRequestList.add(friendRequestDto);
            }
            requestListDto.setFriendRequestList(friendRequestList);
            return requestListDto;
        } else {
            throw new IllegalArgumentException("User " + username + " not found");
        }
    }

    public FriendListDto getFriendList(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            ArrayList<String> friendList = new ArrayList<String>();
            UserEntity user = retrievedUser.get();
            for (UserEntity friend : user.getFriends()) {
                friendList.add(friend.getUsername());
            }
            FriendListDto friendListDto = new FriendListDto();
            friendListDto.setFriends(friendList);
            return friendListDto;
        } else {
            throw new IllegalArgumentException("User " + username + " could not be found");
        }
    }
}
