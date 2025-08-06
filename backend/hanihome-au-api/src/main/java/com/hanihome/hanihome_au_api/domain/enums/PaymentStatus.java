package com.hanihome.hanihome_au_api.domain.enums;

public enum PaymentStatus {
    PENDING,     // 지불 대기
    PAID,        // 지불 완료
    OVERDUE,     // 연체
    CANCELLED,   // 취소됨
    REFUNDED     // 환불됨
}