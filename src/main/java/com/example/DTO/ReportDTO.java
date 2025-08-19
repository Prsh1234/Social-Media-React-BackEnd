package com.example.DTO;


import java.time.LocalDateTime;


public class ReportDTO {
    private int id;
    private int reporterId;
    private String reporterName;
    private LocalDateTime createdAt;

    // Embed full post details
    private UserPostDTO userPostDTO;

    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserPostDTO getUserPostDTO() { return userPostDTO; }
    public void setUserPostDTO(UserPostDTO userPostDTO) { this.userPostDTO = userPostDTO; }
}
