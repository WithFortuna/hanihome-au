package com.hanihome.hanihome_au_api.domain.enums;

/**
 * 신고 처리 상태를 정의하는 열거형
 */
public enum ReportStatus {
    // 접수 상태
    PENDING("접수됨", "신고가 접수되어 검토 대기 중"),
    
    // 처리 중 상태
    UNDER_REVIEW("검토 중", "관리자가 신고를 검토하고 있음"),
    INVESTIGATING("조사 중", "신고 내용에 대해 상세 조사 중"),
    
    // 완료 상태
    RESOLVED("해결됨", "신고가 처리되어 해결됨"),
    DISMISSED("기각됨", "신고가 기각됨"),
    
    // 조치 상태
    WARNING_ISSUED("경고 발송", "대상에게 경고가 발송됨"),
    CONTENT_REMOVED("콘텐츠 삭제", "신고된 콘텐츠가 삭제됨"),
    USER_SUSPENDED("사용자 정지", "신고된 사용자가 정지됨"),
    ACCOUNT_BANNED("계정 차단", "계정이 영구 차단됨"),
    
    // 기타
    ESCALATED("상급자 전달", "복잡한 사안으로 상급자에게 전달됨"),
    DUPLICATE("중복 신고", "동일한 내용의 중복 신고");

    private final String displayName;
    private final String description;

    ReportStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
    
    public boolean isCompleted() {
        return this == RESOLVED || this == DISMISSED || this == WARNING_ISSUED || 
               this == CONTENT_REMOVED || this == USER_SUSPENDED || this == ACCOUNT_BANNED;
    }
    
    public boolean isActionTaken() {
        return this == WARNING_ISSUED || this == CONTENT_REMOVED || 
               this == USER_SUSPENDED || this == ACCOUNT_BANNED;
    }
}