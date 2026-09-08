package lk.ijse.examsy.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom OAuth2 authorization request resolver that intercepts the authorization request
 * and embeds the user-selected role (?role=student or ?role=teacher) into the OAuth2 state parameter.
 * This guarantees role preservation across the Google OAuth2 redirect round-trip.
 */
@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(req, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(req, request);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest req, HttpServletRequest request) {
        if (req == null) return null;

        // Grab the ?role= parameter sent from the React button
        String role = request.getParameter("role");
        if (role == null || role.isBlank()) {
            role = "student";
        }
        // Normalize role (handle potential typos like 'studnet')
        if (role.toLowerCase().startsWith("teach")) {
            role = "teacher";
        } else {
            role = "student";
        }

        // Embed the role directly into the "state" string that goes to Google
        // e.g., "state=random_string||role:student"
        Map<String, Object> extraParams = new HashMap<>(req.getAdditionalParameters());
        String originalState = req.getState();
        String customState = (originalState != null ? originalState : "") + "||role:" + role;

        return OAuth2AuthorizationRequest.from(req)
                .additionalParameters(extraParams)
                .state(customState)
                .build();
    }
}
