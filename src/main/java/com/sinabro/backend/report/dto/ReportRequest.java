package com.sinabro.backend.report.dto;

// 부모가 자녀 리포트 미리보기 요청 시 사용하는 DTO
public class ReportRequest {
    private String childId;  // 자녀 ID
    private String date;     // 리포트 날짜

    // getter/setter (Lombok을 쓰면 @Data 가능)
    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
