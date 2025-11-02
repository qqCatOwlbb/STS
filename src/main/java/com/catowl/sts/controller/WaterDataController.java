package com.catowl.sts.controller;

import com.catowl.sts.model.DTO.Request.DataUploadRequest;
import com.catowl.sts.model.DTO.Response.MyApiResponse;
import com.catowl.sts.model.DTO.Response.DataQueryResponse;
import com.catowl.sts.model.DTO.Response.DataUploadResponse;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/water-data")
@Api(tags = "3. 水质数据", description = "处理传感器数据的上传和查询")
public class WaterDataController {

    @PostMapping("/upload")
    @ApiOperation(value = "上传水质数据", notes = "供传感器或边缘设备调用，上传单条浊度数据")
    @ApiResponses({
            @ApiResponse(code = 202, message = "数据已接收")
    })
    public ResponseEntity<MyApiResponse<DataUploadResponse>> uploadData(
            @Valid @RequestBody DataUploadRequest dataRequest) {

        // --- Mock Data ---
        DataUploadResponse mockResponse = new DataUploadResponse();
        mockResponse.setStatus("success");
        mockResponse.setDataStrId("data_ulid_mock_789");

        MyApiResponse<DataUploadResponse> response = new MyApiResponse<>("数据已接收", mockResponse);
        // 使用 202 Accepted 表示已接收，正在异步处理
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @GetMapping("/query/{sourceStrId}")
    @ApiOperation(value = "查询水质历史数据", notes = "根据水源ID查询近期的水质数据")
    public ResponseEntity<MyApiResponse<List<DataQueryResponse>>> queryData(
            @ApiParam(value = "水源的字符串ID", example = "source_ulid_mock_abc")
            @PathVariable String sourceStrId,
            @ApiParam(value = "查询开始时间 (ISO 8601)") @RequestParam(required = false) LocalDateTime startTime,
            @ApiParam(value = "查询结束时间 (ISO 8601)") @RequestParam(required = false) LocalDateTime endTime) {

        // --- Mock Data ---
        DataQueryResponse mockData1 = new DataQueryResponse();
        mockData1.setStrId("data_ulid_mock_789");
        mockData1.setTurbidityValue(new BigDecimal("150.75"));
        mockData1.setUnit("NTU");
        mockData1.setMeasuredAt(LocalDateTime.now().minusHours(2));

        DataQueryResponse mockData2 = new DataQueryResponse();
        mockData2.setStrId("data_ulid_mock_790");
        mockData2.setTurbidityValue(new BigDecimal("162.10"));
        mockData2.setUnit("NTU");
        mockData2.setMeasuredAt(LocalDateTime.now().minusHours(1));

        List<DataQueryResponse> mockList = List.of(mockData1, mockData2);
        MyApiResponse<List<DataQueryResponse>> response = new MyApiResponse<>("查询成功", mockList);
        return ResponseEntity.ok(response);
    }
}
