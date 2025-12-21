package com.catowl.sts.service;

import com.catowl.sts.exception.InternetServerException;
import com.catowl.sts.mapper.AuthMapper;
import com.catowl.sts.model.entity.User;
import com.catowl.sts.service.impl.UserServiceImpl;
import com.catowl.sts.utils.RedisCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @program: STS
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-12-21 16:37
 **/
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private AuthMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("rawPassword");
    }

    @Test
    void insertUser_ShouldSucceed_WhenUsernameIsUnique() {
        // Arrange (准备)
        when(userMapper.findByUsername("testUser")).thenReturn(null);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userMapper.insertUser(any(User.class))).thenReturn(1);

        // Act (执行)
        int result = userService.insertUser(testUser);

        // Assert (验证)
        assertEquals(1, result);
        verify(passwordEncoder).encode("rawPassword"); // 验证密码是否被加密
        verify(userMapper).insertUser(argThat(user ->
                user.getStrId() != null && // 验证 ULID 是否生成
                        user.getApiKey() != null && // 验证 API Key 是否生成
                        user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void insertUser_ShouldThrowException_WhenUsernameExists() {
        // Arrange
        when(userMapper.findByUsername("testUser")).thenReturn(new User());

        // Act & Assert
        assertThrows(InternetServerException.class, () -> {
            userService.insertUser(testUser);
        });

        // 验证并没有执行插入操作
        verify(userMapper, never()).insertUser(any(User.class));
    }
}
