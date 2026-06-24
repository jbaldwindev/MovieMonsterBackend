package com.MovieMonster.demo.Services;

import com.MovieMonster.demo.Dto.RequestResponseDto;
import com.MovieMonster.demo.Models.FriendRequest;
import com.MovieMonster.demo.Models.UserEntity;
import com.MovieMonster.demo.Repositories.FriendRequestRepository;
import com.MovieMonster.demo.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendRequestRepository friendRequestRepository;
    @Mock
    private S3Client s3Client;
    @Mock
    private S3Presigner s3Presigner;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(s3Client, s3Presigner);
        ReflectionTestUtils.setField(userService, "userRepository", userRepository);
        ReflectionTestUtils.setField(userService, "friendRequestRepository", friendRequestRepository);
    }

    @Test
    void requestResponseRejectsUserWhoIsNotTheReceiver() {
        UserEntity sender = new UserEntity();
        sender.setUsername("bob");
        UserEntity receiver = new UserEntity();
        receiver.setUsername("victim");
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);

        RequestResponseDto response = new RequestResponseDto();
        response.setRequestId(42);
        response.setIsAccepted(true);
        when(friendRequestRepository.findById(42)).thenReturn(Optional.of(request));

        assertThrows(AccessDeniedException.class,
                () -> userService.handleRequestResponse(response, "alice"));

        verify(userRepository, never()).save(sender);
        verify(userRepository, never()).save(receiver);
        verify(friendRequestRepository, never()).delete(request);
    }
}
