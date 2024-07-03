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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
        //TODO remove dummy data made to test movie list persistence (62-74)
        MovieList movieList = new MovieList();
        ArrayList<MovieRating> movieRatings = new ArrayList<MovieRating>();
        MovieRating movieRating1 = new MovieRating();
        movieRating1.setMovieId(07221);
        movieRating1.setRating(3);

        movieRatings.add(movieRating1);
        movieList.setMovieRatingList(movieRatings);
        user.setMovieList(movieList);
        movieRating1.setMovieList(movieList);
        movieListRepository.save(movieList);
        movieRatingRepository.save(movieRating1);

        Role roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));

        userRepository.save(user);
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
