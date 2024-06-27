package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Dto.MovieInfoDto;
import com.MovieMonster.demo.Dto.MovieListDto;
import com.MovieMonster.demo.Models.DashDisplay;
import com.MovieMonster.demo.Models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.json.*;

import java.time.Duration;
import java.util.ArrayList;

@Service
public class MovieService {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    @Autowired
    private WebClient apiClient;
    @Value("${tmdb.key}")
    private String tmdbKey;

    public MovieInfoDto getMovieInfo(int id) {
        String fullUri = "/3/movie/" + id + "?language=en-US&api_key=" + tmdbKey;
        String jsonResponse = apiClient
                .get()
                .uri(fullUri)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();
        System.out.println(jsonResponse);
        MovieInfoDto movieInfoDto = new MovieInfoDto();
        JSONObject obj = new JSONObject(jsonResponse);
        movieInfoDto.setId(obj.getInt("id"));
        movieInfoDto.setTitle(obj.getString("title"));
        movieInfoDto.setOverview(obj.getString("overview"));
        movieInfoDto.setPosterPath(obj.getString("poster_path"));
        movieInfoDto.setBackdropPath(obj.getString("backdrop_path"));
        return movieInfoDto;
    }

    public MovieListDto fillDash(int page, DashDisplay dashDisplay) {
        String pageNum = String.valueOf(page);
        String fullUri = "";
        switch(dashDisplay) {
            case POPULAR:
                fullUri = "/3/discover/movie?include_adult=false&include_video=false&language=en-US&page="
                        + pageNum
                        + "&sort_by=popularity.desc&api_key="
                        + tmdbKey;
                break;
            case TOP:
                fullUri = "/3/discover/movie?include_adult=false&include_video=false&language=en-US&page="
                        + pageNum
                        + "&sort_by=vote_average.desc&without_genres=99,10755&vote_count.gte=200&api_key="
                        + tmdbKey;
                break;
            case PLAYING:
                fullUri = "/3/movie/now_playing?language=en-US&page=" + pageNum + "&api_key=" + tmdbKey;
                break;
        }
        String jsonResponse = apiClient
                .get()
                .uri(fullUri)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();

        JSONObject obj = new JSONObject(jsonResponse);
        JSONArray results = obj.getJSONArray("results");
        MovieListDto movieListDto = new MovieListDto();
        movieListDto.setMovieList(new ArrayList<Movie>());
        for (int i = 0; i < results.length(); i++) {
            Movie movie = new Movie();
            movie.setId(results.getJSONObject(i).getInt("id"));
            movie.setOriginalTitle(results.getJSONObject(i).getString("original_title"));
            String posterPath = "https://image.tmdb.org/t/p/w500" + results.getJSONObject(i).getString("poster_path");
            movie.setPosterPath(posterPath);
            movie.setOverview(results.getJSONObject(i).getString("overview"));
            ArrayList<Movie> movieList = movieListDto.getMovieList();
            movieList.add(movie);
            movieListDto.setMovieList(movieList);
        }
        return movieListDto;
    }
}
