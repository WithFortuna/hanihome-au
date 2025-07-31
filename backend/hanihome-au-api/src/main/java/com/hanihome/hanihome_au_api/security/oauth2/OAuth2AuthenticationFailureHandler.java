package com.hanihome.hanihome_au_api.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uris:http://localhost:3000/auth/callback}")
    private String[] authorizedRedirectUris;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String targetUrl = getRedirectUri(request);
        
        if (targetUrl == null) {
            targetUrl = authorizedRedirectUris[0]; // Default to first authorized URI
        }

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        log.error("OAuth2 authentication failed: {}", exception.getMessage());
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getRedirectUri(HttpServletRequest request) {
        String redirectUri = request.getParameter("redirect_uri");
        
        if (redirectUri != null && isAuthorizedRedirectUri(redirectUri)) {
            return redirectUri;
        }
        
        return null;
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        for (String authorizedRedirectUri : authorizedRedirectUris) {
            if (uri.startsWith(authorizedRedirectUri)) {
                return true;
            }
        }
        return false;
    }
}