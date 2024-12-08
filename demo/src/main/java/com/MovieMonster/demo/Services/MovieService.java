package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Dto.*;
import com.MovieMonster.demo.Models.*;
import com.MovieMonster.demo.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.json.*;

import java.time.Duration;
import java.util.*;

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
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieCommentRepository movieCommentRepository;
    @Autowired
    private CommentLikeRepository commentLikeRepository;

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

    public void createMovieList(int userId) {
        Optional<UserEntity> retrievedUser = userRepository.findById(userId);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            MovieList movieList = new MovieList();
            ArrayList<MovieRating> movieRatings = new ArrayList<MovieRating>();
            movieList.setMovieRatingList(movieRatings);
            user.setMovieList(movieList);
            movieListRepository.save(movieList);
        }
    }

    public void rateMovie(String username, String title, int movieId, int rating) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            MovieList movieList = user.getMovieList();
            List<MovieRating> movieRatings = movieList.getMovieRatingList();
            boolean movieFound = false;
            for (int i = 0; i < movieRatings.size(); i++) {
                MovieRating movieRating = movieRatings.get(i);
                if (movieRating.getMovieId() == movieId) {
                    movieFound = true;
                    movieRating.setRating(rating);
                    movieRatingRepository.save(movieRating);
                }
            }

            if (!movieFound) {
                MovieRating movieRating = new MovieRating();
                movieRating.setMovieId(movieId);
                movieRating.setMovieList(movieList);
                movieRating.setTitle(title);
                movieRating.setRating(rating);
                movieRatingRepository.save(movieRating);
            }

        }
    }

    public MovieRatingDto checkRating(String username, Integer movieId) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        UserEntity user;
        if (retrievedUser.isPresent()) {
            user = retrievedUser.get();
            Collection<MovieRating> ratingList = user.getMovieList().getMovieRatingList();
            MovieRating retrievedRating = ratingList
                    .stream()
                    .filter(rating -> movieId.equals(rating.getMovieId()))
                    .findFirst()
                    .orElse(null);

            MovieRatingDto movieRatingDto = new MovieRatingDto();
            movieRatingDto.setMovieRating(retrievedRating.getRating());
            return movieRatingDto;
        }
        return null;
    }

    public ArrayList<MovieRatingDto> getUserMovieList(String username, SortOrder sortOrder) {
        Optional<UserEntity> retrievedUser = userRepository.findByUsername(username);
        if (retrievedUser.isPresent()) {
            UserEntity user = retrievedUser.get();
            List<MovieRating> movieRatingList = user.getMovieList().getMovieRatingList();
            Collections.sort(movieRatingList, new Comparator<MovieRating>() {
                public int compare(MovieRating m1, MovieRating m2) {
                    return m2.getRating().compareTo(m1.getRating());
                }
            });
            if (sortOrder == SortOrder.ASC) {
                Collections.reverse(movieRatingList);
            }
            ArrayList<MovieRatingDto> movieRatingArrList = new ArrayList<MovieRatingDto>();
            for (MovieRating rating : movieRatingList) {
                MovieRatingDto movieRatingDto = new MovieRatingDto();
                movieRatingDto.setMovieId(rating.getMovieId());
                movieRatingDto.setMovieTitle(rating.getTitle());
                movieRatingDto.setMovieRating(rating.getRating());
                movieRatingArrList.add(movieRatingDto);
            }
            return movieRatingArrList;
        } else {
            return null;
        }
    }

    //TODO Update this to take a parameter for page, have it return list of 5 comments at a time.
    //TODO add a username param to the request
    //TODO update the request to take in a json object with the movies id as well as the user's username
    //TODO update the frontend to use the new version of the request instead
    public CommentListDto getCommentList(CommentRequestDto commentRequestDto) {
        Optional<Movie> fetchedMovie = movieRepository.findByMovieId(commentRequestDto.getMovieId());
        if (fetchedMovie.isPresent()) {
            Movie movie = fetchedMovie.get();
            CommentListDto commentListDto = new CommentListDto();
            commentListDto.setMovieId(commentRequestDto.getMovieId());
            ArrayList<MovieCommentDto> movieCommentArrList = new ArrayList<MovieCommentDto>();
            for (MovieComment comment : movie.getMovieCommentList()) {
                MovieCommentDto newCommentDto = new MovieCommentDto();
                newCommentDto.setMovieId(comment.getMovie().getMovieId());
                newCommentDto.setUsername(comment.getUsername());
                newCommentDto.setCommentId(comment.getId());
                newCommentDto.setComment(comment.getMovieComment());
                newCommentDto.setLikeCount(comment.getCommentLikeList().size());
                Boolean usernameFound = false;
                for (CommentLike commentLike : comment.getCommentLikeList()) {
                    if (commentLike.getUsername().equals(commentRequestDto.getUsername())) {
                        usernameFound = true;
                    }
                }
                newCommentDto.setCurrentUserLiked(usernameFound);
                movieCommentArrList.add(newCommentDto);
            }
            commentListDto.setCommentList(movieCommentArrList);
            if (commentListDto.getCommentList().size() > 1) {
            }
            return commentListDto;
        }
        return new CommentListDto();
    }

    public void likeComment(CommentLikeDto commentLikeDto) {
        //retrieve the movie comment by the comment id passed in
        int commentId = commentLikeDto.getCommentId();
        Optional<MovieComment> fetchedMovieComment = movieCommentRepository.findById(commentId);
        if (fetchedMovieComment.isPresent()) {
            MovieComment movieComment = fetchedMovieComment.get();
            List<CommentLike> commentLikeList = movieComment.getCommentLikeList();
            //TODO instead of just returning here, remove this code, and have a separate
            //dislike comment function delete the comment like
            for (CommentLike cLike : commentLikeList) {
                if (cLike.getUsername().equals(commentLikeDto.getUsername())) {
                    return;
                }
            }
            CommentLike commentLike = new CommentLike();
            //create a CommentLike object, set the username and comment
            commentLike.setUsername(commentLikeDto.getUsername());
            commentLike.setComment(movieComment);
            //save the CommentLike object in the repository
            commentLikeRepository.save(commentLike);
            //add the CommentLike to the list of CommentLikes in the comment object

            commentLikeList.add(commentLike);
            movieComment.setCommentLikeList(commentLikeList);
            //save the comment in the comment repository
            movieCommentRepository.save(movieComment);
        }
    }

    public void unlikeComment(CommentLikeDto commentLikeDto) {
        Optional<MovieComment> fetchedMovieComment = movieCommentRepository.findById(commentLikeDto.getCommentId());
        if (fetchedMovieComment.isPresent()) {
            MovieComment movieComment = fetchedMovieComment.get();
            List<CommentLike> commentLikeList = movieComment.getCommentLikeList();
            for (int i = 0; i < commentLikeList.size(); i++) {
                if (commentLikeList.get(i).getUsername().equals(commentLikeDto.getUsername())) {
                    CommentLike commentLike = commentLikeList.get(i);
                    System.out.println("Comment like username: " + commentLike.getUsername());
                    System.out.println("Comment Like id: " + commentLike.getId());
                    commentLikeRepository.deleteById(commentLike.getId());
                    commentLikeList.remove(i);
                    i--;
                }
            }
            movieComment.setCommentLikeList(commentLikeList);
            movieCommentRepository.save(movieComment);

        }
    }

    public void postComment(MovieCommentDto movieCommentDto) {
        int movieId = movieCommentDto.getMovieId();
        Optional<Movie> fetchedMovie = movieRepository.findByMovieId(movieId);
        if (fetchedMovie.isPresent()) {
            Movie movie = fetchedMovie.get();
            //create the comment
            MovieComment movieComment = new MovieComment();
            movieComment.setMovie(movie);
            movieComment.setMovieComment(movieCommentDto.getComment());
            movieComment.setUsername(movieCommentDto.getUsername());
            //save to the comment respository
            movieCommentRepository.save(movieComment);
            //add the comment to the movie's comment list
            List<MovieComment> movieCommentList = movie.getMovieCommentList();
            movieCommentList.add(movieComment);
            movie.setMovieCommentList(movieCommentList);
            //save the movie in the movie repository
            movieRepository.save(movie);
        } else {
            //create the movie
            MovieInfoDto movieInfoDto = getMovieInfo(movieCommentDto.getMovieId());
            Movie movie = new Movie();
            movie.setMovieId(movieId);
            movie.setOriginalTitle(movieInfoDto.getTitle());
            movie.setOverview(movieInfoDto.getOverview());
            movie.setMovieCommentList(new ArrayList<MovieComment>());
            movie.setPosterPath(movieInfoDto.getPosterPath());
            //create the comment
            MovieComment movieComment = new MovieComment();
            movieComment.setMovieComment(movieCommentDto.getComment());
            movieComment.setUsername(movieCommentDto.getUsername());
            movieComment.setMovie(movie);
            //add the comment to the movies comment list
            List<MovieComment> movieCommentList = new ArrayList<MovieComment>();
            movieCommentList.add(movieComment);
            movie.setMovieCommentList(movieCommentList);
            //save the movie to the movie repository
            movieRepository.save(movie);
            //save the comment to the comment repository
            movieCommentRepository.save(movieComment);
        }

    }

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
