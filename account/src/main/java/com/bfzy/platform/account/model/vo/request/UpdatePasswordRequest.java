package com.bfzy.platform.account.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求.
 *
 * @author zhangyu
 */
@Data
public class UpdatePasswordRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
