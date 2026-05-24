package com.bfzy.platform.account.model.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料响应.
 *
 * @author zhangyu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountProfileResponse {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;
}
