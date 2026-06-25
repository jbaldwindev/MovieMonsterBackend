package com.MovieMonster.demo.Security;

import com.MovieMonster.demo.Repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(SecurityConfigTest.TestConfig.class)
@WebAppConfiguration
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:3000",
        "app.cors.allowed-origin-patterns=http://localhost:*",
        "jwt.secret=test-jwt-secret"
})
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JWTGenerator jwtGenerator;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void cookieAuthenticatedMutationRequiresCsrfToken() throws Exception {
        authenticatedJwtCookie();

        mockMvc.perform(post("/api/user/bio")
                        .cookie(new Cookie("accessToken", "valid-token"))
                        .contentType("application/json")
                        .content("""
                                {"bio":"hello"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void cookieAuthenticatedMutationWithCsrfTokenCanReachController() throws Exception {
        authenticatedJwtCookie();

        mockMvc.perform(post("/api/user/bio")
                        .with(csrf())
                        .cookie(new Cookie("accessToken", "valid-token"))
                        .contentType("application/json")
                        .content("""
                                {"bio":"hello"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedGetIssuesReadableCsrfCookie() throws Exception {
        authenticatedJwtCookie();

        mockMvc.perform(get("/api/user/csrf-check")
                        .cookie(new Cookie("accessToken", "valid-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    private void authenticatedJwtCookie() {
        UserDetails userDetails = User.withUsername("alice")
                .password("password")
                .roles("USER")
                .build();
        when(jwtGenerator.validateToken("valid-token")).thenReturn(true);
        when(jwtGenerator.getUsernameFromJWT("valid-token")).thenReturn("alice");
        when(customUserDetailsService.loadUserByUsername("alice")).thenReturn(userDetails);
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class TestConfig {
        @Bean
        TestMutationController testMutationController() {
            return new TestMutationController();
        }

        @Bean
        CustomUserDetailsService customUserDetailsService() {
            return mock(CustomUserDetailsService.class);
        }

        @Bean
        JwtAuthEntryPoint jwtAuthEntryPoint() {
            return mock(JwtAuthEntryPoint.class);
        }

        @Bean
        JWTGenerator jwtGenerator() {
            return mock(JWTGenerator.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean
        SecurityConstants securityConstants() {
            return mock(SecurityConstants.class);
        }
    }

    @RestController
    static class TestMutationController {
        @PostMapping("/api/user/bio")
        String mutate(Authentication authentication) {
            return authentication.getName();
        }

        @GetMapping("/api/user/csrf-check")
        String csrfCheck(Authentication authentication) {
            return authentication.getName();
        }
    }
}
