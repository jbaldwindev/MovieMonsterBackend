package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.AuthResponseDto;
import com.MovieMonster.demo.Dto.LoginDto;
import com.MovieMonster.demo.Dto.RegisterDto;
import com.MovieMonster.demo.Models.MovieList;
import com.MovieMonster.demo.Models.MovieRating;
import com.MovieMonster.demo.Models.UserEntity;
import com.MovieMonster.demo.Models.Role;
import com.MovieMonster.demo.Repositories.MovieListRepository;
import com.MovieMonster.demo.Repositories.MovieRatingRepository;
import com.MovieMonster.demo.Repositories.RoleRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import com.MovieMonster.demo.Security.JWTGenerator;
import com.MovieMonster.demo.Services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")
public class AuthController {

    private UserRepository userRepository;
    @Autowired
    private MovieListRepository movieListRepository;
    @Autowired
    private MovieRatingRepository movieRatingRepository;
    @Autowired
    private MovieService movieService;
    private AuthenticationManager authenticationManager;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JWTGenerator jwtGenerator;
    @Autowired
    public AuthController(UserRepository userRepository, AuthenticationManager authenticationManager,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                          JWTGenerator jwtGenerator) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username is taken", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setBio("User has not yet written their bio (but you can view their list to get a sense of what they enjoy!)");
        Role roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));
        user.setJoinDate(LocalDateTime.now());
        userRepository.save(user);
        movieService.createMovieList(user.getId());
        return new ResponseEntity<>("User has been registered", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);
        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }
}
