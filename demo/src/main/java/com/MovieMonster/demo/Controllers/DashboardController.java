package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Dto.MovieListDto;
import com.MovieMonster.demo.Services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/dash")
public class DashboardController {

    @Autowired
    MovieService movieService;

    @GetMapping("/popular/{page}")
    public ResponseEntity<MovieListDto> getPopular(@PathVariable int page) {
        MovieListDto movieList = movieService.getPopular(page);
        return new ResponseEntity<>(movieList, HttpStatus.OK);
    }
}
