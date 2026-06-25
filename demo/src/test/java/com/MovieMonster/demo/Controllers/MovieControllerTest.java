package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Services.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
    }

    @Test
    void rateMovieUsesAuthenticatedUserInsteadOfPayloadUsername() throws Exception {
        mockMvc.perform(post("/api/movie/rate")
                        .principal(authentication("alice"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "victim",
                                  "movieTitle": "Alien",
                                  "movieId": 348,
                                  "movieRating": 5
                                }
                                """))
                .andExpect(status().isOk());

        verify(movieService).rateMovie("alice", "Alien", 348, 5);
        verify(movieService, never()).rateMovie("victim", "Alien", 348, 5);
    }

    @Test
    void deleteRatingDeletesOnlyAuthenticatedUsersRating() throws Exception {
        when(movieService.deleteRatingOwnedBy(42, "alice")).thenReturn(false);

        mockMvc.perform(delete("/api/movie/delete-rating/victim/42")
                        .principal(authentication("alice")))
                .andExpect(status().isNotFound());

        verify(movieService).deleteRatingOwnedBy(42, "alice");
        verify(movieService, never()).deleteRating(42);
    }

    private UsernamePasswordAuthenticationToken authentication(String username) {
        return new UsernamePasswordAuthenticationToken(username, null);
    }
}
