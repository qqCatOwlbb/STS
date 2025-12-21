package com.catowl.sts.service;

/**
 * @program: STS
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-12-21 16:42
 **/
import com.alibaba.fastjson.JSON;
import com.catowl.sts.controller.AuthController;
import com.catowl.sts.model.dto.Request.UserLoginRequest;
import com.catowl.sts.model.entity.User;
import com.catowl.sts.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        // 初始化 MockMvc，独立测试 Controller，不加载 Spring Context
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        UserLoginRequest loginRequest = new UserLoginRequest("testUser", "password123");
        String mockToken = "eyJhbGciOiJIUzI1Ni...";

        when(userService.login(any(User.class))).thenReturn(mockToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data").value(mockToken));
    }

    // 如果要测试参数校验（例如 @Valid），需要集成测试或手动触发 Validator，
    // 在 standaloneSetup 模式下通常不做深度校验测试。
}
