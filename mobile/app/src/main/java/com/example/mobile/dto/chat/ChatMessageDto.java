package com.example.mobile.dto.chat;

public class ChatMessageDto {
    private long id;
    private long senderId;
    private long receiverId;
    private String content;
    private String timestamp;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSenderId() { return senderId; }
    public void setSenderId(long senderId) { this.senderId = senderId; }
    public long getReceiverId() { return receiverId; }
    public void setReceiverId(long receiverId) { this.receiverId = receiverId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}