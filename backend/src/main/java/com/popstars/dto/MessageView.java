package com.popstars.dto;

/**
 * 一条客服留言，以及管理员的回复。
 */
public class MessageView {

    public Long id;
    public String username;
    public String nickname;
    public String content;
    public String reply;
    public String createdAt;
    public String repliedAt;
}
