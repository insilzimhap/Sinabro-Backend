package com.sinabro.backend.report.dto;

public class ReportResponseDto {
    private String reportText;
    private int reward;

    public ReportResponseDto(String reportText, int reward) {
        this.reportText = reportText;
        this.reward = reward;
    }

    // Getters
    public String getReportText() { return reportText; }
    public int getReward() { return reward; }
}