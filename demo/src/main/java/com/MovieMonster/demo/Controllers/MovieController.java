package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.*;
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
@RequestMapping("/api/movie")
public class MovieController {
    @Autowired
    private MovieService movieService;
    @GetMapping("/{id}")
    public MovieInfoDto getMovie(@PathVariable int id) {
        return movieService.getMovieInfo(id);
    }

    @PostMapping("/post-comment")
    public ResponseEntity<String> postMovieComment(@RequestBody MovieCommentDto movieCommentDto) {
        movieService.postComment(movieCommentDto);
        return new ResponseEntity<>("Comment posted!", HttpStatus.OK);
    }

    @PostMapping("/like-comment")
    public ResponseEntity<String> likeComment(@RequestBody CommentLikeDto commentLikeDto) {
        System.out.println("initial request received");
        movieService.likeComment(commentLikeDto);
        return new ResponseEntity<>("Comment liked!", HttpStatus.OK);
    }

    @PostMapping("/unlike-comment")
    public ResponseEntity<String> unlikeComment(@RequestBody CommentLikeDto commentLikeDto) {
        System.out.println("unlike request received");
        //TODO call function in movieService
        movieService.unlikeComment(commentLikeDto);
        return new ResponseEntity<>("Like removed from comment", HttpStatus.OK);
    }

    @GetMapping("/get-comments/{username}&filmId={movieId}")
    public CommentListDto getComments(@PathVariable String username, @PathVariable int movieId) {
        CommentRequestDto commentRequestDto = new CommentRequestDto(movieId, username);
        return movieService.getCommentList(commentRequestDto);
    }

    @GetMapping("/search/{title}")
    public MovieListDto searchMovie(@PathVariable String title) {
        return movieService.searchMovie(title);
    }

    @GetMapping("/search/{title}/{page}")
    public MovieListDto searchMovieAdvanced(@PathVariable String title, @PathVariable int page) {
        return movieService.advancedSearch(title, page);
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

    @DeleteMapping("/delete-rating/{username}/{ratingId}")
    public ResponseEntity<String> deleteRating(@PathVariable String username, @PathVariable int ratingId) {
        System.out.println("Deleting rating with id " + ratingId);
        movieService.deleteRating(username, ratingId);
        return new ResponseEntity<>("Rating deleted!", HttpStatus.OK);
    }

    @GetMapping("/check-rating/{username}/{movieId}")
    public ResponseEntity<MovieRatingDto> checkRating(@PathVariable String username, @PathVariable int movieId) {
        MovieRatingDto movieRatingDto;
        try {
            movieRatingDto = movieService.checkRating(username, movieId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            movieRatingDto = null;
        }
        if (movieRatingDto != null) {
            return new ResponseEntity<>(movieRatingDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

}
