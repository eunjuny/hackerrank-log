package com.hackerrank.log.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * HackerRank 제출 요청 DTO
 */
public class SubmissionRequest {
    
    @NotBlank(message = "문제명은 필수입니다")
    private String problemName;
    
    @NotBlank(message = "코드는 필수입니다")
    private String code;
    
    @NotBlank(message = "언어는 필수입니다")
    private String language;
    
    private String challengeId;
    private String difficulty;
    private String[] tags;
    private String timestamp;
    
    // Constructors
    public SubmissionRequest() {
    }
    
    public SubmissionRequest(String problemName, String code, String language) {
        this.problemName = problemName;
        this.code = code;
        this.language = language;
    }
    
    // Getters and Setters
    public String getProblemName() {
        return problemName;
    }
    
    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getChallengeId() {
        return challengeId;
    }
    
    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String[] getTags() {
        return tags;
    }
    
    public void setTags(String[] tags) {
        this.tags = tags;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "SubmissionRequest{" +
                "problemName='" + problemName + '\'' +
                ", language='" + language + '\'' +
                ", challengeId='" + challengeId + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}

