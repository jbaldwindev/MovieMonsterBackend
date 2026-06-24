package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.*;
import com.MovieMonster.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/add-friend/{username}")
    public ResponseEntity<String> AddFriend(@PathVariable String username, @RequestParam("friend") String friendUsername,
                                            Authentication authentication) {
        requireOwnAccount(username, authentication);
        userService.addFriend(username, friendUsername);
        return new ResponseEntity<>("Friend added!", HttpStatus.OK);
    }

    @PostMapping("/request-response")
    public ResponseEntity<String> RespondRequest(@RequestBody RequestResponseDto requestResponseDto,
                                                 Authentication authentication) {
        userService.handleRequestResponse(requestResponseDto, authentication.getName());
        return new ResponseEntity<>("Friend request response handled!", HttpStatus.OK);
    }

    @PostMapping("/send-request")
    public ResponseEntity<String> SendRequest(@RequestBody SendRequestDto sendRequestDto,
                                              Authentication authentication) {
        userService.sendRequest(authentication.getName(), sendRequestDto.getReceiverUsername());
        return new ResponseEntity<>("Request sent!", HttpStatus.OK);
    }

    @GetMapping("/received-requests/{username}")
    public RequestListDto GetReceivedRequests(@PathVariable String username) {
        return userService.getReceivedRequests(username);
    }

    @GetMapping("/{user1}/get-friend-status/{user2}")
    public FriendStatusDto GetFriendStatus(@PathVariable String user1, @PathVariable String user2) {
        return userService.getFriendStatus(user1, user2);
    }

    @GetMapping("/sent-requests/{username}")
    public RequestListDto GetSentRequests(@PathVariable String username) {
        return userService.getSentRequests(username);
    }

    @DeleteMapping("{sender}/requests/{receiver}")
    public ResponseEntity<String> deleteFriendRequest(@PathVariable String sender,
                                                      @PathVariable String receiver,
                                                      Authentication authentication) {
        requireOwnAccount(sender, authentication);
        userService.deleteFriendRequest(sender, receiver);
        return new ResponseEntity<>("Request successfully deleted", HttpStatus.OK);
    }

    @DeleteMapping("{user1}/friends/{user2}")
    public ResponseEntity<String> RemoveFriend(@PathVariable String user1, @PathVariable String user2,
                                               Authentication authentication) {
        requireOwnAccount(user1, authentication);
        userService.removeFriend(user1, user2);
        return new ResponseEntity<>("Friend successfully removed!", HttpStatus.OK);
    }

    @GetMapping("/get-friends/{username}")
    public FriendListDto GetFriendList(@PathVariable String username) {
        return userService.getFriendList(username);
    }

    @GetMapping("{searcher}/search-users/{username}")
    public SearchListDto GetUserSearch(@PathVariable String searcher, @PathVariable String username) {
        return userService.getUserSearch(searcher, username);
    }

    @PostMapping("/upload-icon/{username}")
    public ResponseEntity<String> UploadIcon(@RequestParam("file") MultipartFile file, @PathVariable String username,
                                             Authentication authentication) {
        requireOwnAccount(username, authentication);
        return userService.UploadIcon(file, username);
    }

    @GetMapping("/icon/{username}")
    public ResponseEntity<String> GetIcon(@PathVariable String username) {
        return userService.getIcon(username);
    }

    @GetMapping("/profile/{username}")
    public ProfileInfoDto GetProfileInfo(@PathVariable String username) {
        return userService.getProfileInfo(username);
    }

    @PostMapping("/{username}/favorites/add")
    public ResponseEntity<String> AddFavorite(@PathVariable String username, @RequestParam int movieId,
                                              Authentication authentication) {
        requireOwnAccount(username, authentication);
        return userService.addFavorite(username, movieId);
    }

    @DeleteMapping("/{username}/favorites/remove")
    public ResponseEntity<String> RemoveFavorite(@PathVariable String username, @RequestParam int movieId,
                                                 Authentication authentication) {
        requireOwnAccount(username, authentication);
        return userService.removeFavorite(username, movieId);
    }

    @GetMapping("/{username}/favorites/all")
    public ResponseEntity<List<Integer>> GetFavorites(@PathVariable String username) {
        return userService.getFavorites(username);
    }

    @PostMapping("/favorites/rank")
    public ResponseEntity<String> ChangeFavoritesRanking(@RequestBody RankingRequestDto rankingRequestDto,
                                                         Authentication authentication) {
        return userService.changeFavoritesRanking(
                authentication.getName(),
                rankingRequestDto.getMovieId(),
                rankingRequestDto.getRankingDirection()
        );
    }

    @PostMapping("/bio")
    public ResponseEntity<String> SetBio(@RequestBody BioDto bioDto, Authentication authentication) {
        return userService.setBio(authentication.getName(), bioDto.getBio());
    }

    @GetMapping("/{username}/bio")
    public ResponseEntity<String> GetBio(@PathVariable String username) {
        return userService.getBio(username);
    }

    @GetMapping("/auth/user-exists/{username}")
    public ResponseEntity<Boolean> IsUsernameTaken(@PathVariable String username) { return userService.isUsernameTaken(username); }

    private void requireOwnAccount(String username, Authentication authentication) {
        if (!authentication.getName().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another user's account");
        }
    }
}
