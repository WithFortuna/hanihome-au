package com.hanihome.hanihome_au_api.domain.enums;

/**
 * 신고 유형을 정의하는 열거형
 */
public enum ReportType {
    // 사용자 관련 신고
    INAPPROPRIATE_BEHAVIOR("부적절한 행동"),
    HARASSMENT("괴롭힘"),
    SPAM_USER("스팸 사용자"),
    FAKE_PROFILE("가짜 프로필"),
    
    // 매물 관련 신고
    FAKE_LISTING("가짜 매물"),
    MISLEADING_INFO("허위 정보"),
    INAPPROPRIATE_CONTENT("부적절한 콘텐츠"),
    DUPLICATE_LISTING("중복 매물"),
    OVERPRICED("과도한 가격"),
    
    // 거래 관련 신고
    FRAUDULENT_TRANSACTION("사기 거래"),
    BREACH_OF_CONTRACT("계약 위반"),
    PAYMENT_ISSUE("결제 문제"),
    
    // 리뷰 관련 신고
    FAKE_REVIEW("가짜 리뷰"),
    INAPPROPRIATE_LANGUAGE("부적절한 언어"),
    SPAM_REVIEW("스팸 리뷰"),
    
    // 기타
    TECHNICAL_ISSUE("기술적 문제"),
    OTHER("기타");

    private final String description;

    ReportType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}