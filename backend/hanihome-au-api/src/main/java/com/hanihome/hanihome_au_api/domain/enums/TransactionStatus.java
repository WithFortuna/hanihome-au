package com.hanihome.hanihome_au_api.domain.enums;

public enum TransactionStatus {
    PROPOSED,       // 제안됨
    NEGOTIATING,    // 협상 중
    APPROVED,       // 승인됨
    CONTRACT_PENDING, // 계약 대기
    COMPLETED,      // 완료됨
    CANCELLED       // 취소됨
}