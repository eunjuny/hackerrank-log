package com.hackerrank.log.dto;

/**
 * HackerRank 제출 응답 DTO
 */
public class SubmissionResponse {
    
    private boolean success;
    private String message;
    private String filePath;
    private String gitMessage;
    
    // Constructors
    public SubmissionResponse() {
    }
    
    public SubmissionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public SubmissionResponse(boolean success, String message, String filePath) {
        this.success = success;
        this.message = message;
        this.filePath = filePath;
    }
    
    public SubmissionResponse(boolean success, String message, String filePath, String gitMessage) {
        this.success = success;
        this.message = message;
        this.filePath = filePath;
        this.gitMessage = gitMessage;
    }
    
    // Static factory methods
    public static SubmissionResponse success(String message, String filePath, String gitMessage) {
        return new SubmissionResponse(true, message, filePath, gitMessage);
    }
    
    public static SubmissionResponse failure(String message) {
        return new SubmissionResponse(false, message);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getGitMessage() {
        return gitMessage;
    }
    
    public void setGitMessage(String gitMessage) {
        this.gitMessage = gitMessage;
    }
}

