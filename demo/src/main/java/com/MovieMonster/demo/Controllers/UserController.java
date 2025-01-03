package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.RequestListDto;
import com.MovieMonster.demo.Dto.SendRequestDto;
import com.MovieMonster.demo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;
    //TODO implement function to handle accepting/denying friend request

    @PostMapping("/add-friend/{username}")
    public ResponseEntity<String> AddFriend(@PathVariable String username, @RequestParam("friend") String friendUsername) {
        userService.addFriend(username, friendUsername);
        return new ResponseEntity<String>("Friend added!", HttpStatus.OK);
    }

    @PostMapping("/send-request")
    public ResponseEntity<String> SendRequest(@RequestBody SendRequestDto sendRequestDto) {
        userService.sendRequest(sendRequestDto.getSenderUsername(), sendRequestDto.getReceiverUsername());
        return new ResponseEntity<String>("Request sent!", HttpStatus.OK);
    }

    //TODO implement get sent requests
    @GetMapping("/received-requests/{username}")
    public RequestListDto GetReceivedRequests(@PathVariable String username) {
        return userService.getReceivedRequests(username);
    }
    //TODO implement check request sent/or check friend status
}
