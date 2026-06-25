package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Repositories.RoleRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import com.MovieMonster.demo.Security.JWTGenerator;
import com.MovieMonster.demo.Services.MovieService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTGenerator jwtGenerator;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "cookieSecure", false);
        ReflectionTestUtils.setField(authController, "cookieSameSite", "Lax");
        ReflectionTestUtils.setField(authController, "cookieDomain", "");
        ReflectionTestUtils.setField(authController, "movieService", movieService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void meReturnsUnauthorizedWhenAuthenticationIsMissing() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshReturnsUnauthorizedWhenRefreshTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutClearsAuthCookies() throws Exception {
        mockMvc.perform(post("/api/auth/logout").secure(true))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    void refreshReturnsOkWhenRefreshTokenIsValid() throws Exception {
        when(jwtGenerator.validateToken("refresh-token")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("refresh-token")).thenReturn("monster");
        when(jwtGenerator.generateTokenFromUsername("monster", 900000)).thenReturn("access-token");

        mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andExpect(cookie().exists("accessToken"));
    }
}
