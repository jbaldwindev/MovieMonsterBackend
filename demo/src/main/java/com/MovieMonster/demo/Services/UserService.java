package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Dto.*;
import com.MovieMonster.demo.Models.FriendRequest;
import com.MovieMonster.demo.Models.RequestStatus;
import com.MovieMonster.demo.Models.UserEntity;
import com.MovieMonster.demo.Repositories.FriendRequestRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
        } else if (sender.getFriends().contains(receiver)) {
            throw new IllegalArgumentException("Users are already friends");
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

    public SearchListDto getUserSearch(String username) {
        List<UserEntity> searchedUsers =  userRepository.findSimilarUsernames(username);
        ArrayList<String> usernames = new ArrayList<>();
        if (!searchedUsers.isEmpty()) {
            for (UserEntity user : searchedUsers) {
                usernames.add(user.getUsername());
            }
        }
        SearchListDto searchListDto = new SearchListDto();
        searchListDto.setUsernames(usernames);
        return searchListDto;
    }

    public FriendStatusDto getFriendStatus(String user1Name, String user2Name) {
        Optional<UserEntity> retrievedUser1 = userRepository.findByUsername(user1Name);
        Optional<UserEntity> retrievedUser2 = userRepository.findByUsername(user2Name);
        if (retrievedUser1.isPresent() && retrievedUser2.isPresent()) {
            FriendStatusDto friendStatus = new FriendStatusDto();
            UserEntity user1 = retrievedUser1.get();
            UserEntity user2 = retrievedUser2.get();
            Boolean requestExists = false;
            Boolean isFriend = false;
            friendStatus.setIsFriend(isFriend);
            friendStatus.setRequestPending(requestExists);
            if (user1.getFriends().contains(user2)) {
                friendStatus.setIsFriend(true);
            } else if (friendRequestRepository.existsBySenderAndReceiver(user1, user2)) {
                friendStatus.setRequestPending(true);
                friendStatus.setReceiverUsername(user2Name);
                friendStatus.setSenderUsername(user1Name);
            } else if (friendRequestRepository.existsBySenderAndReceiver(user2, user1)) {
                friendStatus.setRequestPending(true);
                friendStatus.setReceiverUsername(user1Name);
                friendStatus.setSenderUsername(user2Name);
            }
            return friendStatus;
        } else {
            throw new IllegalArgumentException("User(s) do not exist");
        }
    }

    public void deleteFriendRequest(String senderUsername, String receiverUsername) {
        Optional<UserEntity> retrievedSender = userRepository.findByUsername(senderUsername);
        Optional <UserEntity> retrievedReceiver = userRepository.findByUsername(receiverUsername);
        if (retrievedReceiver.isPresent() && retrievedSender.isPresent()) {
            UserEntity sender = retrievedSender.get();
            UserEntity receiver = retrievedReceiver.get();
            Optional<FriendRequest> retrievedRequest =
                    friendRequestRepository.findBySenderAndReceiver(sender, receiver);
            if (retrievedRequest.isPresent()) {
                FriendRequest friendRequest = retrievedRequest.get();
                friendRequestRepository.delete(friendRequest);
                sender.getSentRequests().remove(friendRequest);
                receiver.getReceivedRequests().remove(friendRequest);
                userRepository.save(sender);
                userRepository.save(receiver);
            } else {
                throw new IllegalArgumentException("Request between these users does not exist");
            }
        } else {
            throw new IllegalArgumentException("Either sender or receiver does not exist");
        }
    }

    public void handleRequestResponse(RequestResponseDto requestResponseDto) {
        Optional<FriendRequest> retrievedRequest = friendRequestRepository.findById(requestResponseDto.getRequestId());
        if (retrievedRequest.isPresent()) {
            FriendRequest friendRequest = retrievedRequest.get();
            UserEntity sender = friendRequest.getSender();
            UserEntity receiver = friendRequest.getReceiver();
            if (requestResponseDto.getIsAccepted()) {
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

    public void removeFriend(String user1Name, String user2Name) {
        Optional<UserEntity> retrievedUser1 = userRepository.findByUsername(user1Name);
        Optional<UserEntity> retrievedUser2 = userRepository.findByUsername(user2Name);
        if (retrievedUser1.isPresent() && retrievedUser2.isPresent()) {
            UserEntity user1 = retrievedUser1.get();
            UserEntity user2 = retrievedUser2.get();
            user1.getFriends().remove(user2);
            user2.getFriends().remove(user1);
            userRepository.save(user1);
            userRepository.save(user1);
        } else {
            throw new IllegalArgumentException("One of the users does not exist!");
        }
    }

    public RequestListDto getSentRequests(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            RequestListDto requestListDto = new RequestListDto();
            ArrayList<FriendRequestDto> friendRequestList = new ArrayList<>();
            for (FriendRequest friendRequest : user.getSentRequests()) {
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
