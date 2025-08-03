package com.hanihome.hanihome_au_api.domain.user.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {
    GOOGLE("google", "Google", "https://accounts.google.com"),
    KAKAO("kakao", "Kakao", "https://kauth.kakao.com"),
    APPLE("apple", "Apple", "https://appleid.apple.com");

    private final String registrationId;
    private final String displayName;
    private final String authorizationUri;

    public static OAuthProvider fromRegistrationId(String registrationId) {
        for (OAuthProvider provider : values()) {
            if (provider.registrationId.equals(registrationId)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown OAuth provider: " + registrationId);
    }

    public boolean isGoogle() {
        return this == GOOGLE;
    }

    public boolean isKakao() {
        return this == KAKAO;
    }

    public boolean isApple() {
        return this == APPLE;
    }
}