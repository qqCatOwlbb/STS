package com.catowl.sts.controller;

import com.catowl.sts.model.DTO.Request.WaterSourceCreateRequest;
import com.catowl.sts.model.DTO.Request.WaterSourceUpdateRequest;
import com.catowl.sts.model.DTO.Response.MyApiResponse;
import com.catowl.sts.model.DTO.Response.WaterSourceResponse;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/water-sources")
@Api(tags = "2. 水源信息 (Dify聊天)", description = "管理用户的水源监测点 (即 Dify 聊天会话)")
public class WaterSourceController {

    @PostMapping
    @ApiOperation(value = "创建新水源 (聊天)", notes = "创建一个新的水源监测点，并初始化 Dify 会话")
    @ApiResponses({
            @ApiResponse(code = 201, message = "创建成功")
    })
    public ResponseEntity<MyApiResponse<WaterSourceResponse>> createWaterSource(
            @Valid @RequestBody WaterSourceCreateRequest createRequest) {

        // --- Mock Data ---
        WaterSourceResponse mockSource = new WaterSourceResponse();
        mockSource.setStrId("source_ulid_mock_abc");
        mockSource.setSourceName(createRequest.getSourceName());
        mockSource.setSourceType(createRequest.getSourceType());
        mockSource.setDescription(createRequest.getDescription());
        mockSource.setDifyConversationId("dify_conv_mock_xyz987");
        mockSource.setCreatedAt(LocalDateTime.now());
        mockSource.setActive(true);

        MyApiResponse<WaterSourceResponse> response = new MyApiResponse<>("水源创建成功", mockSource);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @ApiOperation(value = "分页获取当前用户的水源", notes = "使用游标分页 (cursor pagination) 列出当前用户的所有水源监测点")
    public ResponseEntity<MyApiResponse<List<WaterSourceResponse>>> getAllWaterSources(
            @ApiParam(value = "上一页最后一条记录的 strId (首页查询则不传)", example = "source_ulid_mock_page1_item2")
            @RequestParam(required = false) String lastStrId,
            @ApiParam(value = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {

        // --- Mock Data (模拟分页) ---
        List<WaterSourceResponse> mockList;

        if (lastStrId == null) {
            // 模拟第一页数据
            WaterSourceResponse mockSource1 = new WaterSourceResponse();
            mockSource1.setStrId("source_ulid_mock_page1_item1");
            mockSource1.setSourceName("模拟-工业排污口-A");
            mockSource1.setSourceType("INDUSTRIAL");
            mockSource1.setDifyConversationId("dify_conv_mock_xyz987");
            mockSource1.setCreatedAt(LocalDateTime.now().minusDays(5));
            mockSource1.setActive(true);

            WaterSourceResponse mockSource2 = new WaterSourceResponse();
            mockSource2.setStrId("source_ulid_mock_page1_item2");
            mockSource2.setSourceName("模拟-生活污水-B");
            mockSource2.setSourceType("DOMESTIC");
            mockSource2.setDifyConversationId("dify_conv_mock_abc123");
            mockSource2.setCreatedAt(LocalDateTime.now().minusDays(4));
            mockSource2.setActive(true);

            // 假设 pageSize 足够大，一次返回2条
            mockList = List.of(mockSource1, mockSource2);
        } else {
            // 模拟第二页数据 (假设 lastStrId 是 "source_ulid_mock_page1_item2")
            WaterSourceResponse mockSource3 = new WaterSourceResponse();
            mockSource3.setStrId("source_ulid_mock_page2_item1");
            mockSource3.setSourceName("模拟-农业灌溉-C");
            mockSource3.setSourceType("AGRICULTURAL");
            mockSource3.setDifyConversationId("dify_conv_mock_def456");
            mockSource3.setCreatedAt(LocalDateTime.now().minusDays(3));
            mockSource3.setActive(false);

            mockList = Collections.singletonList(mockSource3);

            // 如果 lastStrId 是 "source_ulid_mock_page2_item1"，模拟第三页（空）
            if (lastStrId.equals("source_ulid_mock_page2_item1")) {
                mockList = Collections.emptyList();
            }
        }

        MyApiResponse<List<WaterSourceResponse>> response = new MyApiResponse<>("获取成功", mockList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sourceStrId}")
    @ApiOperation(value = "获取特定水源详情", notes = "根据 strId 获取单个水源的详细信息")
    public ResponseEntity<MyApiResponse<WaterSourceResponse>> getWaterSourceById(
            @ApiParam(value = "水源的字符串ID", example = "source_ulid_mock_abc")
            @PathVariable String sourceStrId) {

        // --- Mock Data ---
        WaterSourceResponse mockSource = new WaterSourceResponse();
        mockSource.setStrId(sourceStrId);
        mockSource.setSourceName("模拟-工业排污口");
        mockSource.setSourceType("INDUSTRIAL");
        mockSource.setDescription("这是一个模拟的水源");
        mockSource.setDifyConversationId("dify_conv_mock_xyz987");
        mockSource.setCreatedAt(LocalDateTime.now().minusDays(5));
        mockSource.setActive(true);

        MyApiResponse<WaterSourceResponse> response = new MyApiResponse<>("获取成功", mockSource);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sourceStrId}")
    @ApiOperation(value = "更新水源信息", notes = "更新特定水源的名称、描述或激活状态")
    public ResponseEntity<MyApiResponse<WaterSourceResponse>> updateWaterSource(
            @ApiParam(value = "水源的字符串ID", example = "source_ulid_mock_abc")
            @PathVariable String sourceStrId,
            @Valid @RequestBody WaterSourceUpdateRequest updateRequest) {

        // --- Mock Data ---
        WaterSourceResponse mockSource = new WaterSourceResponse();
        mockSource.setStrId(sourceStrId);
        mockSource.setSourceName(updateRequest.getSourceName() != null ? updateRequest.getSourceName() : "模拟-工业排污口(已更新)");
        mockSource.setSourceType("INDUSTRIAL");
        mockSource.setDescription(updateRequest.getDescription() != null ? updateRequest.getDescription() : "描述已更新");
        mockSource.setDifyConversationId("dify_conv_mock_xyz987");
        mockSource.setCreatedAt(LocalDateTime.now().minusDays(5));
        mockSource.setActive(updateRequest.getIsActive() != null ? updateRequest.getIsActive() : true);

        MyApiResponse<WaterSourceResponse> response = new MyApiResponse<>("更新成功", mockSource);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sourceStrId}")
    @ApiOperation(value = "删除水源", notes = "删除一个水源监测点 (及其关联的 Dify 会话)")
    public ResponseEntity<MyApiResponse<String>> deleteWaterSource(
            @ApiParam(value = "水源的字符串ID", example = "source_ulid_mock_abc")
            @PathVariable String sourceStrId) {

        // --- Mock Logic ---
        MyApiResponse<String> response = new MyApiResponse<>("删除成功", sourceStrId);
        return ResponseEntity.ok(response);
    }
}
