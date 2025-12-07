package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Dto.*;
import com.MovieMonster.demo.Models.FriendRequest;
import com.MovieMonster.demo.Models.RankingDirection;
import com.MovieMonster.demo.Models.RequestStatus;
import com.MovieMonster.demo.Models.UserEntity;
import com.MovieMonster.demo.Repositories.FriendRequestRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public UserService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

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

    public ResponseEntity<Boolean> isUsernameTaken(String username) {
        boolean usernameTaken = userRepository.existsByUsername(username);
        return new ResponseEntity<>(usernameTaken, HttpStatus.OK);
    }

    public SearchListDto getUserSearch(String requestingUsername, String searchedUsername) {
        List<UserEntity> searchedUsers =  userRepository.findSimilarUsernames(searchedUsername);
        ArrayList<FriendStatusDto> connectionStatusList = new ArrayList<>();
        if (!searchedUsers.isEmpty()) {
            for (UserEntity user : searchedUsers) {
                if (requestingUsername.equals(user.getUsername())) continue;
                FriendStatusDto friendStatusDto = getFriendStatus(requestingUsername, user.getUsername());
                friendStatusDto.setRequestedUsername(user.getUsername());
                friendStatusDto.setSearchedUserIcon("http://localhost:8080/api/user/icon/" + user.getUsername());
                connectionStatusList.add(friendStatusDto);
            }
        }
        SearchListDto searchListDto = new SearchListDto();
        searchListDto.setUserConnections(connectionStatusList);
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
                friendRequestDto.setSenderIcon("http://localhost:8080/api/user/icon/" + friendRequest.getSender().getUsername());
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
                friendRequestDto.setReceiverIcon("http://localhost:8080/api/user/icon/" + friendRequest.getReceiver().getUsername());
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

    public ResponseEntity<String> UploadIcon(MultipartFile file, String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        UserEntity user = retrievedUser.get();

        try {
            String key = "profile-icons/" + username + "-" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .acl("public-read")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

            user.setIcon(key);
            userRepository.save(user);

            return ResponseEntity.ok("S3 upload completed: " + key);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }


    public FriendListDto getFriendList(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            ArrayList<FriendDto> friendList = new ArrayList<>();
            UserEntity user = retrievedUser.get();
            for (UserEntity friend : user.getFriends()) {
                FriendDto friendDto = new FriendDto();
                friendDto.setUsername(friend.getUsername());
                friendDto.setIconPath("http://localhost:8080/api/user/icon/" + friend.getUsername());
                friendList.add(friendDto);
            }
            FriendListDto friendListDto = new FriendListDto();
            friendListDto.setFriends(friendList);
            return friendListDto;
        } else {
            throw new IllegalArgumentException("User " + username + " could not be found");
        }
    }

    public ResponseEntity<String> getIcon(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = retrievedUser.get();

        String key = (user.getIcon() == null)
                ? "default.jpg"
                : user.getIcon();

        try {
            // generate presigned URL valid for 10 minutes
            GetObjectRequest getObjectReq = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(getObjectReq)
                    .build();

            PresignedGetObjectRequest presignedObject = s3Presigner.presignGetObject(presignRequest);

            return ResponseEntity.ok(presignedObject.url().toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    public ProfileInfoDto getProfileInfo(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            ProfileInfoDto profileInfoDto = new ProfileInfoDto();
            profileInfoDto.setUsername(user.getUsername());
            if (user.getJoinDate() == null) {
                user.setJoinDate(LocalDateTime.now());
                userRepository.save(user);
            }
            if (user.getBio() == null) {
                user.setBio("User has not yet written their bio (but you can view their list to get a sense of what they enjoy!)");
                userRepository.save(user);
            }
            profileInfoDto.setBio(user.getBio());
            profileInfoDto.setJoinDate(user.getJoinDate());
            profileInfoDto.setFriendCount(user.getFriends().size());
            profileInfoDto.setFavoriteIds(user.getFavorites());
            return profileInfoDto;
        }
        return new ProfileInfoDto();
    }
    public ResponseEntity<String> addFavorite(String username, int movieId) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            List<Integer> favorites = user.getFavorites();
            if (favorites != null && favorites.size() >= 10) {
                return new ResponseEntity<>("User has reached max limit of favorites (10)", HttpStatus.BAD_REQUEST);
            } else {
                if (favorites == null) {
                    favorites = new ArrayList<>();
                }
                if (favorites.contains(movieId)) {
                    return new ResponseEntity<>("Movie is already in favorites", HttpStatus.BAD_REQUEST);
                }
                favorites.add(movieId);
                user.setFavorites(favorites);
                userRepository.save(user);
                return new ResponseEntity<>("Movie successfully added to favorites", HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> removeFavorite(String username, int movieId) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            List<Integer> favorites = user.getFavorites();
            if (favorites.size() < 1) {
                return new ResponseEntity<>("User has no favorites to remove", HttpStatus.BAD_REQUEST);
            } else {
                for (Integer fav : favorites) {
                    if (fav.equals(movieId)) {
                        favorites.remove(fav);
                        user.setFavorites(favorites);
                        userRepository.save(user);
                        return new ResponseEntity<>("Successfully removed from favorites", HttpStatus.OK);
                    }
                }
                return new ResponseEntity<>("Movie not found in favorites list", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<List<Integer>> getFavorites(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            return new ResponseEntity<>(user.getFavorites(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> setBio(BioDto bioDto) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(bioDto.getUsername());
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            user.setBio(bioDto.getBio());
            userRepository.save(user);
            return new ResponseEntity<>("Bio successfully updated!", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> getBio(String username) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();

            if (user.getBio() == null || user.getBio().isEmpty()) {
                user.setBio("User has not yet written their bio (but you can view their list to get a sense of what they enjoy!)");
                userRepository.save(user);
                return new ResponseEntity<>(user.getBio(), HttpStatus.OK);
            }

            String bio = user.getBio();
            return new ResponseEntity<>(bio, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found!", HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> changeFavoritesRanking(String username, int movieId, RankingDirection rankDirection) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            List<Integer> favoritesList = user.getFavorites();
            int filmIndex = favoritesList.indexOf(movieId);
            if (filmIndex == 0 && rankDirection == RankingDirection.UP) {
                return new ResponseEntity<>("Film is already ranked #1 in your favorites", HttpStatus.BAD_REQUEST);
            }

            if (filmIndex >= 0) {
                if (rankDirection == RankingDirection.UP) {
                    int upperFilmIndex = filmIndex - 1;
                    favoritesList.set(filmIndex, favoritesList.get(upperFilmIndex));
                    favoritesList.set(upperFilmIndex, movieId);
                } else if (rankDirection == RankingDirection.DOWN) {
                    if (filmIndex == favoritesList.size() - 1) {
                        return new ResponseEntity<>("Film is already ranked last in your favorites list", HttpStatus.BAD_REQUEST);
                    }
                    int lowerFilmIndex = filmIndex + 1;
                    favoritesList.set(filmIndex, favoritesList.get(lowerFilmIndex));
                    favoritesList.set(lowerFilmIndex, movieId);
                }
                user.setFavorites(favoritesList);
                userRepository.save(user);
                return new ResponseEntity<>("Favorites ranking successfully changed", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Movie not currently in favorites list", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }
    }
}
