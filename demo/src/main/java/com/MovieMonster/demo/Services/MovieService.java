package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Dto.CastMemberDto;
import com.MovieMonster.demo.Dto.MovieInfoDto;
import com.MovieMonster.demo.Dto.MovieListDto;
import com.MovieMonster.demo.Dto.MovieSearchDto;
import com.MovieMonster.demo.Models.*;
import com.MovieMonster.demo.Repositories.MovieListRepository;
import com.MovieMonster.demo.Repositories.MovieRatingRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.json.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);
    @Autowired
    private WebClient apiClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieListRepository movieListRepository;
    @Autowired
    private MovieRatingRepository movieRatingRepository;
    @Value("${tmdb.key}")
    private String tmdbKey;

    public MovieListDto searchMovie(String title) {
        String fullUri = "https://api.themoviedb.org/3/search/movie?query="
                + title
                + "&include_adult=false&language=en-US&page=1&api_key="
                + tmdbKey;
        String jsonResponse = apiClient
                .get()
                .uri(fullUri)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();
        MovieListDto movieListDto = new MovieListDto();
        ArrayList<MovieSearchDto> movieSearchList = new ArrayList<MovieSearchDto>();
        JSONObject obj = new JSONObject(jsonResponse);
        JSONArray jsonArray = obj.getJSONArray("results");
        int maxResults = 5;
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i >= maxResults) {
                break;
            }
            JSONObject movieJsonObj = jsonArray.getJSONObject(i);
            MovieSearchDto movieSearchDto = new MovieSearchDto();
            movieSearchDto.setId(movieJsonObj.getInt("id"));
            movieSearchDto.setTitle(movieJsonObj.getString("title"));
            movieSearchList.add(movieSearchDto);
        }
        movieListDto.setMovieSearchList(movieSearchList);
        return movieListDto;
    }

    //TODO user creation
    //params: userId

    //user is created
    //calls this function after
    //retrieve user
    //create novie list
    //save movie list
    //add movie list to user
    //save user
    public void createMovieList(int userId) {
        Optional<UserEntity> retrievedUser = userRepository.findById(userId);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            System.out.println("Retrieved user's ID: " + user.getId());
            MovieList movieList = new MovieList();
            ArrayList<MovieRating> movieRatings = new ArrayList<MovieRating>();
            movieList.setMovieRatingList(movieRatings);
            user.setMovieList(movieList);
            movieListRepository.save(movieList);
        }
    }

    public void rateMovie(String username, int movieId, int rating) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            MovieList movieList = user.getMovieList();
            List<MovieRating> movieRatings = movieList.getMovieRatingList();
            boolean movieFound = false;
            for (int i = 0; i < movieRatings.size(); i++) {
                MovieRating movieRating = movieRatings.get(i);
                if (movieRating.getMovieId() == movieId) {
                    System.out.println("the movie was rated once before");
                    movieFound = true;
                    movieRating.setRating(rating);
                    movieRatingRepository.save(movieRating);
                }
            }

            if (!movieFound) {
                MovieRating movieRating = new MovieRating();
                movieRating.setMovieId(movieId);
                movieRating.setMovieList(movieList);
                movieRating.setRating(rating);
                movieRatingRepository.save(movieRating);
            }

        }
    }

    //TODO user opens list page
    //get all rating objects where list matches user's list id
    //store in list
    //return list

    public MovieInfoDto getMovieInfo(int id) {
        String fullUri = "/3/movie/" + id + "?language=en-US&api_key=" + tmdbKey + "&append_to_response=credits";
        String jsonResponse = apiClient
                .get()
                .uri(fullUri)
                .exchange()
                .block()
                .bodyToMono(String.class)
                .block();
        MovieInfoDto movieInfoDto = new MovieInfoDto();
        JSONObject obj = new JSONObject(jsonResponse);
        JSONArray castJsonArray = obj.getJSONObject("credits").getJSONArray("cast");
        ArrayList<CastMemberDto> castMembers = new ArrayList<CastMemberDto>();
        movieInfoDto.setId(obj.getInt("id"));
        movieInfoDto.setTitle(obj.getString("title"));
        movieInfoDto.setOverview(obj.getString("overview"));
        movieInfoDto.setPosterPath(obj.getString("poster_path"));
        movieInfoDto.setBackdropPath(obj.getString("backdrop_path"));
        for (int i = 0; i < castJsonArray.length(); i++) {
            JSONObject castMemberJson = castJsonArray.getJSONObject(i);
            CastMemberDto castMemberDto = new CastMemberDto();
            castMemberDto.setId(castMemberJson.getInt("id"));
            castMemberDto.setName(castMemberJson.getString("name"));
            castMemberDto.setCharacter(castMemberJson.getString("character"));
            if (!castMemberJson.isNull("profile_path")) {
                castMemberDto.setProfilePath(castMemberJson.getString("profile_path"));
            }
            castMembers.add(castMemberDto);
        }
        movieInfoDto.setCast(castMembers);
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
