package com.popstars.dto;

/**
 * 返回给前端的用户信息。不包含密码和令牌。
 */
public class UserView {

    public Long id;
    public String username;
    public String nickname;
    public String role;
    public String createdAt;
}
