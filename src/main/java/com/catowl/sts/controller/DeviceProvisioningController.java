package com.catowl.sts.controller;

import com.catowl.sts.model.dto.Request.DataUploadRequest;
import com.catowl.sts.model.dto.Request.DevicePairRequest;
import com.catowl.sts.model.dto.Request.DeviceRegisterRequest;
import com.catowl.sts.model.dto.Response.DeviceConfigResponse;
import com.catowl.sts.model.dto.Response.MyApiResponse;
import com.catowl.sts.model.dto.Response.UnclaimedDeviceResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Api(tags = "6. 设备配网（Mock）")
@RestController
public class DeviceProvisioningController {
    // --- 日志 ---
    private static final Logger logger = LoggerFactory.getLogger(DeviceProvisioningController.class);
    // --- 【新增】测试专用的硬编码凭证 ---
    private static final String TEST_DEVICE_API_KEY = "test_key_123456789";
    private static final String TEST_SOURCE_STR_ID = "ulid_source_for_test_only";
    private static final String API_KEY_PREFIX = "ApiKey ";

    @PostMapping("/public/api/devices/register")
    @ApiOperation(value = "ESP32 设备注册 (公开)", notes = "当设备未配置时，调用此接口向后端“报到”。")
    public ResponseEntity<MyApiResponse<String>> registerDevice(
            @Valid @RequestBody DeviceRegisterRequest request) {

        // --- Mock 逻辑 ---
        System.out.println("[Mock] 收到设备注册请求: " + request.getMacAddress());
        // (真实逻辑: 将 macAddress 存入 'unclaimed_devices' 表)
        // --- 结束 Mock ---

        return new ResponseEntity<>(new MyApiResponse<>("设备注册成功", null), HttpStatus.CREATED);
    }

    @GetMapping("/public/api/devices/config/{macAddress}")
    @ApiOperation(value = "ESP32 轮询配置 (公开)", notes = "设备定期调用此接口，检查自己是否已被用户“认领”并绑定。")
    public ResponseEntity<DeviceConfigResponse> getDeviceConfig(
            @ApiParam(value = "设备的 MAC 地址", example = "aabbcc112233")
            @PathVariable String macAddress) {

        // --- Mock 逻辑 ---
        logger.info("[Mock] 设备 {} 正在轮询配置...", macAddress);

        // 始终返回硬编码的测试凭证，以便 ESP32 可以获取并保存它们
        logger.info("[Mock] -> 始终返回硬编码的测试凭证。");
        DeviceConfigResponse mockResponse = new DeviceConfigResponse(
                TEST_DEVICE_API_KEY,
                TEST_SOURCE_STR_ID
        );
        return ResponseEntity.ok(mockResponse);
        // --- 结束 Mock ---
    }

    // --- 【新增】测试上传接口 ---
    @PostMapping("/public/api/devices/test-upload")
    @ApiOperation(value = "ESP32 测试数据上传 (公开)", notes = "一个无数据库、仅用于日志记录的测试端点。")
    public ResponseEntity<MyApiResponse<String>> testUploadData(
            @Valid @RequestBody DataUploadRequest dataRequest,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        logger.info("===== [TEST UPLOAD] 收到测试数据 =====");

        // 1. 简单的认证检查
        if (authHeader == null || !authHeader.startsWith(API_KEY_PREFIX)) {
            logger.warn("[TEST UPLOAD] 认证失败: 缺少 'Authorization: ApiKey ...' 头。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String receivedKey = authHeader.substring(API_KEY_PREFIX.length());

        // 2. 比较硬编码的 Key
        if (!Objects.equals(receivedKey, TEST_DEVICE_API_KEY)) {
            logger.warn("[TEST UPLOAD] 认证失败: API Key 不匹配。");
            logger.warn("  预期: {}", TEST_DEVICE_API_KEY);
            logger.warn("  收到: {}", receivedKey);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 3. 认证成功, 打印数据到日志
        logger.info("[TEST UPLOAD] 认证成功!");
        logger.info("  SourceStrId: {}", dataRequest.getSourceStrId());
        logger.info("  Turbidity: {} {}", dataRequest.getTurbidityValue(), dataRequest.getUnit());
        logger.info("  MeasuredAt: {}", dataRequest.getMeasuredAt());
        logger.info("========================================");

        return ResponseEntity.ok(new MyApiResponse<>("测试数据接收成功", null));
    }


    @GetMapping("/api/devices/unclaimed")
    @ApiOperation(value = "获取未认领的设备列表 (私有)", notes = "用户登录后，调用此接口查看网络上待绑定的新设备。")
    public ResponseEntity<MyApiResponse<List<UnclaimedDeviceResponse>>> getUnclaimedDevices() {

        // --- Mock 逻辑 ---
        System.out.println("[Mock] 用户正在获取未认领设备列表...");
        // (真实逻辑: 查询 'unclaimed_devices' 表)
        List<UnclaimedDeviceResponse> mockList = Arrays.asList(
                new UnclaimedDeviceResponse("aabbcc112211", LocalDateTime.now().minusMinutes(5), "设备 (aabbcc)"),
                new UnclaimedDeviceResponse("ffbbdd334455", LocalDateTime.now().minusMinutes(2), "设备 (ffbbdd)")
        );
        // --- 结束 Mock ---

        return ResponseEntity.ok(new MyApiResponse<>("获取成功", mockList));
    }

    @PostMapping("/api/devices/pair")
    @ApiOperation(value = "绑定设备到水源 (私有)", notes = "用户在前端选择“设备”和“水源”后，调用此接口完成绑定。")
    public ResponseEntity<MyApiResponse<String>> pairDevice(
            @Valid @RequestBody DevicePairRequest request) {

        // --- Mock 逻辑 ---
        // (真实逻辑:
        //   1. 从 SecurityContext 获取 userId
        //   2. 验证 userId 是否拥有 request.sourceStrId
        //   3. 找到 'devices' 表中 macAddress 对应的记录
        //   4. 【关键】生成一个唯一的 'device_api_key' 并存储
        //   5. 将 'user_id' 和 'source_id' 存入该记录
        //   6. 将设备状态从 'PENDING' -> 'ACTIVE' )
        System.out.println("[Mock] 用户正在绑定设备: " + request.getMacAddress() +
                " -> 水源: " + request.getSourceStrId());
        // --- 结束 Mock ---

        return ResponseEntity.ok(new MyApiResponse<>("设备绑定成功", null));
    }
}
