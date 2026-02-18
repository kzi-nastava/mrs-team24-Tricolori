package com.example.mobile.dto.chat;

public class SendMessageRequest {
    private long senderId;
    private long receiverId;
    private String content;

    public SendMessageRequest(long senderId, long receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public long getSenderId() { return senderId; }
    public long getReceiverId() { return receiverId; }
    public String getContent() { return content; }
}