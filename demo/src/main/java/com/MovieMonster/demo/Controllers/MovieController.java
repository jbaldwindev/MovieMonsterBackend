package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.MovieInfoDto;
import com.MovieMonster.demo.Dto.MovieListDto;
import com.MovieMonster.demo.Dto.MovieRatingDto;
import com.MovieMonster.demo.Models.MovieRating;
import com.MovieMonster.demo.Services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/movie")
public class MovieController {
    @Autowired
    private MovieService movieService;
    @GetMapping("/{id}")
    public MovieInfoDto getMovie(@PathVariable int id) {
        return movieService.getMovieInfo(id);
    }

    @GetMapping("/search/{title}")
    public MovieListDto searchMovie(@PathVariable String title) {
        return movieService.searchMovie(title);
    }

    @PostMapping("/rate")
    public ResponseEntity<String> rateMovie(@RequestBody MovieRatingDto movieRatingDto) {
        System.out.println("Request has been received");
        movieService.rateMovie(
                movieRatingDto.getUsername(),
                movieRatingDto.getMovieTitle(),
                movieRatingDto.getMovieId(),
                movieRatingDto.getMovieRating());
        return new ResponseEntity<>("Movie Rating updated!", HttpStatus.OK);
    }

    @GetMapping("/list/{username}")
    public ArrayList<MovieRatingDto> getUserMovieList(@PathVariable String username) {
        return movieService.getUserMovieList(username);
    }

}
