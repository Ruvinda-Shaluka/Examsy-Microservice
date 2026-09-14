package lk.ijse.examsy.auth.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lk.ijse.examsy.auth.entity.Role;
import lk.ijse.examsy.auth.entity.UserAccount;
import lk.ijse.examsy.auth.event.UserRegisteredEvent;
import lk.ijse.examsy.auth.kafka.AuthEventProducer;
import lk.ijse.examsy.auth.repository.UserAccountRepo;
import lk.ijse.examsy.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

/**
 * Enterprise OAuth2 Authentication Success Handler.
 * Intercepts successful Google OAuth2 logins, synchronizes user accounts in examsy_auth_db,
 * emits Kafka user lifecycle events for profile provisioning, mints JWTs, and redirects to frontend.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserAccountRepo userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthEventProducer authEventProducer;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("Google OAuth2 authentication success for email: [{}]", email);

        // 1. Extract requested role from state query param (embedded by CustomOAuth2AuthorizationRequestResolver)
        String state = request.getParameter("state");
        String requestedRoleString = "student";
        if (state != null && state.contains("||role:")) {
            requestedRoleString = state.substring(state.indexOf("||role:") + 7);
        }

        Role requestedRole = requestedRoleString.equalsIgnoreCase("teacher") ? Role.TEACHER : Role.STUDENT;

        // 2. Generate safe base username from email
        String baseUsername = (email != null && email.contains("@")) ? email.split("@")[0] : "user_" + UUID.randomUUID().toString().substring(0, 8);

        // 3. Find or create user account
        UserAccount userAccount = userAccountRepository.findByUsernameOrEmail(email, email).orElse(null);

        if (userAccount == null) {
            String candidateUsername = baseUsername;
            if (userAccountRepository.existsByUsername(candidateUsername)) {
                candidateUsername = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 4);
            }

            userAccount = UserAccount.builder()
                    .username(candidateUsername)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(requestedRole)
                    .authProvider("GOOGLE")
                    .isActive(true)
                    .build();

            userAccount = userAccountRepository.save(userAccount);
            log.info("Provisioned new Google OAuth2 user account: username=[{}], role=[{}]", userAccount.getUsername(), userAccount.getRole());

            // 4. Publish UserRegisteredEvent to Kafka for Profile & Notification services
            UserRegisteredEvent.UserRegisteredEventBuilder eventBuilder = UserRegisteredEvent.builder()
                    .userId(userAccount.getId())
                    .username(userAccount.getUsername())
                    .email(userAccount.getEmail())
                    .role(userAccount.getRole().name())
                    .fullName(name != null && !name.isBlank() ? name : userAccount.getUsername())
                    .registeredAt(LocalDateTime.now());

            if (requestedRole == Role.STUDENT) {
                eventBuilder.studentIdentificationNumber(generateStudentIndexNumber());
            } else {
                eventBuilder.instructorId(generateCorporateInstructorId());
            }

            authEventProducer.publishUserRegistered(eventBuilder.build());
        } else {
            // Update auth provider if user originally registered locally
            if ("LOCAL".equalsIgnoreCase(userAccount.getAuthProvider())) {
                userAccount.setAuthProvider("GOOGLE_AND_LOCAL");
                userAccount = userAccountRepository.save(userAccount);
            }
            log.info("Existing user [{}] authenticated via Google OAuth2", userAccount.getUsername());
        }

        // 5. Generate Examsy JWT token with claims
        String jwtToken = jwtUtil.generateToken(userAccount.getUsername(), userAccount.getRole().name(), userAccount.getId());
        String finalRole = userAccount.getRole().name();

        // 6. Redirect to frontend with token and role
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                .queryParam("token", jwtToken)
                .queryParam("role", finalRole)
                .build().toUriString();

        log.info("Redirecting authenticated OAuth2 user to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String generateStudentIndexNumber() {
        int year = Year.now().getValue();
        String uniqueHash = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        return "STU-" + year + "-" + uniqueHash;
    }

    private String generateCorporateInstructorId() {
        int year = Year.now().getValue();
        String uniqueHash = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
        return "EMP-" + year + "-" + uniqueHash;
    }
}
