package com.hanihome.hanihome_au_api.domain.enums;

public enum TransactionActivityType {
    TRANSACTION_CREATED,        // 거래 생성
    OFFER_MADE,                // 제안
    COUNTER_OFFER_MADE,        // 반대 제안
    OFFER_ACCEPTED,            // 제안 수락
    OFFER_REJECTED,            // 제안 거절
    CONTRACT_REQUESTED,        // 계약 요청
    CONTRACT_SIGNED,           // 계약 서명
    CONTRACT_COMPLETED,        // 계약 완료
    TRANSACTION_CANCELLED,     // 거래 취소
    PROPERTY_STATUS_UPDATED,   // 매물 상태 업데이트
    PAYMENT_RECEIVED,          // 결제 수령
    VIEWING_SCHEDULED,         // 뷰잉 일정 잡힘
    VIEWING_COMPLETED,         // 뷰잉 완료
    MESSAGE_SENT,              // 메시지 전송
    DOCUMENT_UPLOADED,         // 문서 업로드
    STATUS_CHANGED             // 상태 변경
}