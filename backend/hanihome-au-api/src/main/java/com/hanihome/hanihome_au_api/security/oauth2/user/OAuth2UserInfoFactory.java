package com.hanihome.hanihome_au_api.security.oauth2.user;

import com.hanihome.hanihome_au_api.domain.enums.OAuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(OAuthProvider.GOOGLE.getRegistrationId())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(OAuthProvider.KAKAO.getRegistrationId())) {
            return new KakaoOAuth2UserInfo(attributes);
        } else {
            throw new IllegalArgumentException("Login with " + registrationId + " is not supported");
        }
    }
}