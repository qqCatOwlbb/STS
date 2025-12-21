package com.catowl.sts.utils;

/**
 * @program: STS
 * @description:
 * @author: qqCatOwlbb
 * @create: 2025-12-21 16:43
 **/
import com.catowl.sts.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void generateAndValidateToken_ShouldSuccess() {
        Long userId = 10086L;

        // 生成 Token
        String token = JwtUtil.generateToken(userId);
        assertNotNull(token);

        // 验证 Token 有效性
        assertTrue(JwtUtil.validateToken(token));

        // 解析 User ID
        String parsedUserId = JwtUtil.getUserIdFromToken(token);
        assertEquals(String.valueOf(userId), parsedUserId);
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenIsGarbage() {
        String invalidToken = "invalid.token.string";

        assertThrows(UnauthorizedException.class, () -> {
            JwtUtil.getUserIdFromToken(invalidToken);
        });
    }
}