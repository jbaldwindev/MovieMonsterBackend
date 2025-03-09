package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.*;
import com.MovieMonster.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/add-friend/{username}")
    public ResponseEntity<String> AddFriend(@PathVariable String username, @RequestParam("friend") String friendUsername) {
        userService.addFriend(username, friendUsername);
        return new ResponseEntity<String>("Friend added!", HttpStatus.OK);
    }

    @PostMapping("/request-response")
    public ResponseEntity<String> RespondRequest(@RequestBody RequestResponseDto requestResponseDto) {
        userService.handleRequestResponse(requestResponseDto);
        return new ResponseEntity<String>("Friend request response handled!", HttpStatus.OK);
    }

    @PostMapping("/send-request")
    public ResponseEntity<String> SendRequest(@RequestBody SendRequestDto sendRequestDto) {
        userService.sendRequest(sendRequestDto.getSenderUsername(), sendRequestDto.getReceiverUsername());
        return new ResponseEntity<String>("Request sent!", HttpStatus.OK);
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
                                                      @PathVariable String receiver) {
        userService.deleteFriendRequest(sender, receiver);
        return new ResponseEntity<String>("Request successfully deleted", HttpStatus.OK);
    }

    @DeleteMapping("{user1}/friends/{user2}")
    public ResponseEntity<String> RemoveFriend(@PathVariable String user1, @PathVariable String user2) {
        userService.removeFriend(user1, user2);
        return new ResponseEntity<String>("Friend successfully removed!", HttpStatus.OK);
    }

    @GetMapping("/get-friends/{username}")
    public FriendListDto GetFriendList(@PathVariable String username) {
        return userService.getFriendList(username);
    }

    @GetMapping("{searcher}/search-users/{username}")
    public SearchListDto GetUserSearch(@PathVariable String searcher, @PathVariable String username) {
        return userService.getUserSearch(searcher, username);
    }

    @PostMapping("/upload-icon")
    public ResponseEntity<String> UploadIcon(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = System.getProperty("user.home") + "/uploads";

            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String filename = file.getOriginalFilename();
            if (filename == null || filename.isEmpty() || filename.equals("blob")) {
                filename = "uploaded_" + System.currentTimeMillis() + ".png";
            }

            File destinationFile = dirPath.resolve(filename).toFile();
            file.transferTo(destinationFile);

            return ResponseEntity.ok("Upload Completed: " + file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}