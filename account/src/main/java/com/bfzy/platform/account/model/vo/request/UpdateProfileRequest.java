package com.bfzy.platform.account.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新用户资料请求.
 *
 * @author zhangyu
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String avatar;

    private String email;

    private String phone;
}
