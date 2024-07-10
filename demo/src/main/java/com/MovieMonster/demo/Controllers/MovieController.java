package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.MovieInfoDto;
import com.MovieMonster.demo.Dto.MovieListDto;
import com.MovieMonster.demo.Dto.MovieRatingDto;
import com.MovieMonster.demo.Models.MovieRating;
import com.MovieMonster.demo.Models.SortOrder;
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
        movieService.rateMovie(
                movieRatingDto.getUsername(),
                movieRatingDto.getMovieTitle(),
                movieRatingDto.getMovieId(),
                movieRatingDto.getMovieRating());
        return new ResponseEntity<>("Movie Rating updated!", HttpStatus.OK);
    }

    @GetMapping("/list/{username}&sort={order}")
    public ArrayList<MovieRatingDto> getUserMovieList(@PathVariable String username, @PathVariable String order) {
        SortOrder sortOrder;
        if (order.equals("asc")) {
            sortOrder = SortOrder.ASC;
        } else {
            sortOrder = SortOrder.DESC;
        }
        return movieService.getUserMovieList(username, sortOrder);
    }

    @GetMapping("/check-rating/{username}/{movieId}")
    public ResponseEntity<MovieRatingDto> checkRating(@PathVariable String username, @PathVariable int movieId) {
        MovieRatingDto movieRatingDto;
        try {
            movieRatingDto = movieService.checkRating(username, movieId);
        } catch (Exception e) {
            movieRatingDto = null;
        }
        if (movieRatingDto != null) {
            return new ResponseEntity<>(movieRatingDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

}
