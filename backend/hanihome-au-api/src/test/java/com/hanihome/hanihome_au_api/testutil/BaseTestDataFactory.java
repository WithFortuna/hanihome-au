package com.hanihome.hanihome_au_api.testutil;

import com.github.javafaker.Faker;
import java.util.Locale;

/**
 * 테스트 데이터 팩토리의 기본 추상 클래스
 * 모든 구체적인 TestDataFactory 클래스들이 상속받아 사용하는 공통 기능을 제공
 */
public abstract class BaseTestDataFactory<T> {
    
    protected static final Faker faker = new Faker(new Locale("ko"));
    
    /**
     * 기본값으로 구성된 엔티티를 생성
     */
    public abstract T createDefault();
    
    /**
     * 최소 필수값으로만 구성된 엔티티를 생성
     */
    public abstract T createMinimal();
    
    /**
     * 최대값/복잡한 구성으로 구성된 엔티티를 생성
     */
    public abstract T createMaximal();
    
    /**
     * 유효하지 않은 데이터를 포함한 엔티티를 생성 (검증 테스트용)
     */
    public abstract T createInvalid();
    
    /**
     * 랜덤한 값으로 구성된 엔티티를 생성
     */
    public abstract T createRandom();
    
    /**
     * 대량 데이터 생성을 위한 컬렉션 생성
     */
    public abstract java.util.List<T> createBulk(int count);
    
    /**
     * 시드값 설정으로 재현 가능한 랜덤 데이터 생성
     */
    public void setSeed(long seed) {
        faker.random().getRandomInternal().setSeed(seed);
    }
    
    /**
     * 한국어 로케일을 사용하는 Faker 인스턴스 반환
     */
    protected Faker getFaker() {
        return faker;
    }
    
    /**
     * 현실적인 한국 주소 생성
     */
    protected String generateKoreanAddress() {
        return faker.address().fullAddress();
    }
    
    /**
     * 한국 전화번호 형식 생성
     */
    protected String generateKoreanPhoneNumber() {
        String[] prefixes = {"010", "011", "016", "017", "018", "019"};
        String prefix = prefixes[faker.random().nextInt(prefixes.length)];
        return prefix + "-" + faker.number().digits(4) + "-" + faker.number().digits(4);
    }
    
    /**
     * 한국 이름 생성
     */
    protected String generateKoreanName() {
        return faker.name().fullName();
    }
    
    /**
     * 부동산 특화 가격 범위 생성
     */
    protected java.math.BigDecimal generateRealisticRentPrice() {
        // 300 ~ 3000 AUD 범위의 현실적인 임대료
        int basePrice = 300 + faker.random().nextInt(2700);
        return new java.math.BigDecimal(basePrice);
    }
    
    /**
     * 부동산 특화 보증금 계산 (임대료의 4-8주)
     */
    protected java.math.BigDecimal calculateBondAmount(java.math.BigDecimal weeklyRent) {
        int weeks = 4 + faker.random().nextInt(5); // 4-8주
        return weeklyRent.multiply(new java.math.BigDecimal(weeks));
    }
}