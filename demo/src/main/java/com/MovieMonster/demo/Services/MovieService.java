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
    private WebClient apiClient;
    private UserRepository userRepository;
    private MovieListRepository movieListRepository;
    private MovieRatingRepository movieRatingRepository;
    private MovieRepository movieRepository;
    private MovieCommentRepository movieCommentRepository;
    private CommentLikeRepository commentLikeRepository;
    private UserService userService;

    public MovieService(
            UserService userService,
            CommentLikeRepository commentLikeRepository,
            MovieCommentRepository movieCommentRepository,
            MovieRepository movieRepository,
            MovieRatingRepository movieRatingRepository,
            MovieListRepository movieListRepository,
            UserRepository userRepository,
            WebClient apiClient
    ) {
        this.userService = userService;
        this.commentLikeRepository = commentLikeRepository;
        this.movieCommentRepository = movieCommentRepository;
        this.movieRepository = movieRepository;
        this.movieRatingRepository = movieRatingRepository;
        this.movieListRepository = movieListRepository;
        this.userRepository = userRepository;
        this.apiClient = apiClient;
    }

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

    public MovieListDto advancedSearch(String title, int page) {
        String fullUri = "https://api.themoviedb.org/3/search/movie?query="
                + title
                + "&include_adult=false&language=en-US&page="
                + page
                + "&api_key="
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
        int totalPages = obj.getInt("total_pages");
        movieListDto.setTotalPages(totalPages);
        if (page > totalPages) {
            return movieListDto;
        }
        JSONArray jsonArray = obj.getJSONArray("results");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject movieJsonObj = jsonArray.getJSONObject(i);
            MovieSearchDto movieSearchDto = new MovieSearchDto();
            movieSearchDto.setId(movieJsonObj.getInt("id"));
            String posterPath;
            try {
                if (movieJsonObj.has("poster_path")&& !movieJsonObj.isNull("poster_path")) {
                    movieSearchDto.setPosterPath("https://image.tmdb.org/t/p/original" + movieJsonObj.getString("poster_path"));
                }
            } catch (Error error) {
                movieSearchDto.setPosterPath("");
            }
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
            movieRatingDto.setRatingId(retrievedRating.getId());
            return movieRatingDto;
        }
        return null;
    }

    public void deleteRating(String username, int ratingId) {
        movieRatingRepository.deleteById(ratingId);
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
                movieRatingDto.setRatingId(rating.getId());
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
                newCommentDto.setUserIconPath(userService.getIcon(comment.getUsername()).getBody());
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
            for (CommentLike cLike : commentLikeList) {
                if (cLike.getUsername().equals(commentLikeDto.getUsername())) {
                    return;
                }
            }
            CommentLike commentLike = new CommentLike();
            commentLike.setUsername(commentLikeDto.getUsername());
            commentLike.setComment(movieComment);
            commentLikeRepository.save(commentLike);
            commentLikeList.add(commentLike);
            movieComment.setCommentLikeList(commentLikeList);
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
            MovieComment movieComment = new MovieComment();
            movieComment.setMovie(movie);
            movieComment.setMovieComment(movieCommentDto.getComment());
            movieComment.setUsername(movieCommentDto.getUsername());
            movieCommentRepository.save(movieComment);
            List<MovieComment> movieCommentList = movie.getMovieCommentList();
            movieCommentList.add(movieComment);
            movie.setMovieCommentList(movieCommentList);
            movieRepository.save(movie);
        } else {
            MovieInfoDto movieInfoDto = getMovieInfo(movieCommentDto.getMovieId());
            Movie movie = new Movie();
            movie.setMovieId(movieId);
            movie.setOriginalTitle(movieInfoDto.getTitle());
            movie.setOverview(movieInfoDto.getOverview());
            movie.setMovieCommentList(new ArrayList<MovieComment>());
            movie.setPosterPath(movieInfoDto.getPosterPath());
            MovieComment movieComment = new MovieComment();
            movieComment.setMovieComment(movieCommentDto.getComment());
            movieComment.setUsername(movieCommentDto.getUsername());
            movieComment.setMovie(movie);
            List<MovieComment> movieCommentList = new ArrayList<MovieComment>();
            movieCommentList.add(movieComment);
            movie.setMovieCommentList(movieCommentList);
            movieRepository.save(movie);
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
        movieInfoDto.setTagline(obj.getString("tagline"));
        movieInfoDto.setReleaseDate(obj.getString("release_date"));
        JSONArray genreJSONData = obj.getJSONArray("genres");
        ArrayList<String> genreList = new ArrayList<String>();
        for (int i = 0; i < genreJSONData.length(); i++) {
            JSONObject genreObj = genreJSONData.getJSONObject(i);
            String genreName = genreObj.getString("name");
            genreList.add(genreName);
        }
        movieInfoDto.setGenres(genreList);
        JSONArray companiesJSONData = obj.getJSONArray("production_companies");
        ArrayList<String> companyList = new ArrayList<String>();
        for (int i = 0; i < companiesJSONData.length(); i++) {
            JSONObject companyObj = companiesJSONData.getJSONObject(i);
            String companyName = companyObj.getString("name");
            companyList.add(companyName);
        }
        movieInfoDto.setProductionCompanies(companyList);
        movieInfoDto.setRuntime(obj.getInt("runtime"));
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
        movieListDto.setTotalPages(obj.getInt("total_pages"));
        movieListDto.setMovieList(new ArrayList<Movie>());
        for (int i = 0; i < results.length(); i++) {
            Movie movie = new Movie();
            movie.setId(results.getJSONObject(i).getInt("id"));
            movie.setOriginalTitle(results.getJSONObject(i).getString("original_title"));
            movie.setEnglishTitle(results.getJSONObject(i).getString("title"));
            if (!results.getJSONObject(i).isNull("poster_path")) {
                String posterPath = "https://image.tmdb.org/t/p/w500" + results.getJSONObject(i).getString("poster_path");
                movie.setPosterPath(posterPath);
            } else {
                movie.setPosterPath("none");
            }
            if (!results.getJSONObject(i).isNull("backdrop_path")) {
                movie.setBackdropPath(results.getJSONObject(i).getString("backdrop_path"));
            } else {
                movie.setBackdropPath("none");
            }
            ArrayList<Movie> movieList = movieListDto.getMovieList();
            movieList.add(movie);
            movieListDto.setMovieList(movieList);
        }
        return movieListDto;
    }
}
