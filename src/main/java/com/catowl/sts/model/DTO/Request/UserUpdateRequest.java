package com.catowl.sts.model.DTO.Request;

import com.catowl.sts.model.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户更新请求体")
public class UserUpdateRequest {

    @Schema(description = "用户名", example = "new_user")
    private String username;

    @Schema(description = "用户密码", example = "password123")
    private String password;

    @Schema(description = "dify的api", example = "key-dsda")
    private String apiKey;

    @ApiModelProperty(
            hidden = true
    )
    public void setUser(User user) {
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setApiKey(this.apiKey);
    }
}
