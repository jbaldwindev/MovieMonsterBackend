package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.MovieListDto;
import com.MovieMonster.demo.Models.DashDisplay;
import com.MovieMonster.demo.Services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "https://moviemonster.xyz")
@RequestMapping("/api/dash")
public class DashboardController {

    @Autowired
    MovieService movieService;

    @GetMapping("/popular/{page}")
    public ResponseEntity<MovieListDto> getPopular(@PathVariable int page) {
        MovieListDto movieList = movieService.fillDash(page, DashDisplay.POPULAR);
        return new ResponseEntity<>(movieList, HttpStatus.OK);
    }

    @GetMapping("/top/{page}")
    public ResponseEntity<MovieListDto> getTopRated(@PathVariable int page) {
        MovieListDto movieList = movieService.fillDash(page, DashDisplay.TOP);
        return new ResponseEntity<>(movieList, HttpStatus.OK);
    }

    @GetMapping("/playing/{page}")
    public ResponseEntity<MovieListDto> getNowPlaying(@PathVariable int page) {
        MovieListDto movieList = movieService.fillDash(page, DashDisplay.PLAYING);
        return new ResponseEntity<>(movieList, HttpStatus.OK);
    }
}
