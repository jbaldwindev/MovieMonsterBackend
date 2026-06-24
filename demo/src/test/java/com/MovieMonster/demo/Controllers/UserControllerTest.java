package com.MovieMonster.demo.Controllers;

import com.MovieMonster.demo.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void usernameExistsEndpointReturnsServiceValue() throws Exception {
        when(userService.isUsernameTaken("monster")).thenReturn(ResponseEntity.ok(true));

        mockMvc.perform(get("/api/user/auth/user-exists/monster"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void sendRequestUsesAuthenticatedUserInsteadOfPayloadSender() throws Exception {
        mockMvc.perform(post("/api/user/send-request")
                        .principal(authentication("alice"))
                        .contentType("application/json")
                        .content("""
                                {"senderUsername":"victim","receiverUsername":"bob"}
                                """))
                .andExpect(status().isOk());

        verify(userService).sendRequest("alice", "bob");
        verify(userService, never()).sendRequest("victim", "bob");
    }

    @Test
    void requestResponsePassesAuthenticatedReceiverToService() throws Exception {
        mockMvc.perform(post("/api/user/request-response")
                        .principal(authentication("alice"))
                        .contentType("application/json")
                        .content("""
                                {"requestId":42,"isAccepted":true}
                                """))
                .andExpect(status().isOk());

        verify(userService).handleRequestResponse(any(), org.mockito.ArgumentMatchers.eq("alice"));
    }

    @Test
    void accountPathMutationsRejectDifferentAuthenticatedUser() throws Exception {
        MockMultipartFile icon = new MockMultipartFile("file", "icon.png", "image/png", new byte[]{1});

        mockMvc.perform(post("/api/user/add-friend/victim")
                        .principal(authentication("alice"))
                        .param("friend", "bob"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/user/victim/requests/bob")
                        .principal(authentication("alice")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/user/victim/friends/bob")
                        .principal(authentication("alice")))
                .andExpect(status().isForbidden());
        mockMvc.perform(multipart("/api/user/upload-icon/victim")
                        .file(icon)
                        .principal(authentication("alice")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/user/victim/favorites/add")
                        .principal(authentication("alice"))
                        .param("movieId", "7"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/user/victim/favorites/remove")
                        .principal(authentication("alice"))
                        .param("movieId", "7"))
                .andExpect(status().isForbidden());

        verify(userService, never()).addFriend(any(), any());
        verify(userService, never()).deleteFriendRequest(any(), any());
        verify(userService, never()).removeFriend(any(), any());
        verify(userService, never()).UploadIcon(any(), any());
        verify(userService, never()).addFavorite(any(), any(Integer.class));
        verify(userService, never()).removeFavorite(any(), any(Integer.class));
    }

    @Test
    void bodyBasedAccountMutationsUseAuthenticatedUser() throws Exception {
        when(userService.setBio(any(), any())).thenReturn(ResponseEntity.ok("updated"));
        when(userService.changeFavoritesRanking(any(), any(Integer.class), any()))
                .thenReturn(ResponseEntity.ok("updated"));

        mockMvc.perform(post("/api/user/bio")
                        .principal(authentication("alice"))
                        .contentType("application/json")
                        .content("""
                                {"username":"victim","bio":"hello"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/user/favorites/rank")
                        .principal(authentication("alice"))
                        .contentType("application/json")
                        .content("""
                                {"username":"victim","movieId":7,"rankingDirection":"UP"}
                                """))
                .andExpect(status().isOk());

        verify(userService).setBio("alice", "hello");
        verify(userService).changeFavoritesRanking("alice", 7,
                com.MovieMonster.demo.Models.RankingDirection.UP);
    }

    private UsernamePasswordAuthenticationToken authentication(String username) {
        return new UsernamePasswordAuthenticationToken(username, null);
    }
}
