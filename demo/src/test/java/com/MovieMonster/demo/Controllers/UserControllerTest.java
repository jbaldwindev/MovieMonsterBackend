package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Repositories.UserRepository;
import com.MovieMonster.demo.Security.CustomUserDetailsService;
import com.MovieMonster.demo.Security.JWTGenerator;
import com.MovieMonster.demo.Security.JwtAuthEntryPoint;
import com.MovieMonster.demo.Security.SecurityConfig;
import com.MovieMonster.demo.Services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JWTGenerator jwtGenerator;

    @MockBean
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    @Test
    void usernameExistsEndpointReturnsServiceValueForAuthenticatedUser() throws Exception {
        when(userService.isUsernameTaken("monster")).thenReturn(ResponseEntity.ok(true));

        mockMvc.perform(get("/api/user/auth/user-exists/monster")
                        .with(user("tester")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
