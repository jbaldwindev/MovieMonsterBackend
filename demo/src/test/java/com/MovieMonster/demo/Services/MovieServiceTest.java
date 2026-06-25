package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Models.MovieList;
import com.MovieMonster.demo.Models.MovieRating;
import com.MovieMonster.demo.Models.UserEntity;
import com.MovieMonster.demo.Repositories.CommentLikeRepository;
import com.MovieMonster.demo.Repositories.MovieCommentRepository;
import com.MovieMonster.demo.Repositories.MovieListRepository;
import com.MovieMonster.demo.Repositories.MovieRatingRepository;
import com.MovieMonster.demo.Repositories.MovieRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private CommentLikeRepository commentLikeRepository;
    @Mock
    private MovieCommentRepository movieCommentRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieRatingRepository movieRatingRepository;
    @Mock
    private MovieListRepository movieListRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WebClient apiClient;

    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(
                userService,
                commentLikeRepository,
                movieCommentRepository,
                movieRepository,
                movieRatingRepository,
                movieListRepository,
                userRepository,
                apiClient
        );
    }

    @Test
    void deleteRatingOwnedByRejectsRatingFromAnotherMovieList() {
        MovieList aliceList = new MovieList();
        aliceList.setId(1);
        UserEntity alice = new UserEntity();
        alice.setUsername("alice");
        alice.setMovieList(aliceList);

        MovieList victimList = new MovieList();
        victimList.setId(2);
        MovieRating victimRating = new MovieRating();
        victimRating.setId(42);
        victimRating.setMovieList(victimList);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(movieRatingRepository.findById(42)).thenReturn(Optional.of(victimRating));

        assertFalse(movieService.deleteRatingOwnedBy(42, "alice"));

        verify(movieRatingRepository, never()).deleteById(42);
        verify(movieRatingRepository, never()).delete(victimRating);
    }
}
