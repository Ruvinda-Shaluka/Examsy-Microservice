package lk.ijse.examsy.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enterprise programmatic RouteLocator configuration for Examsy API Gateway.
 * Explicitly defines routes in code to guarantee zero-fail routing independent of
 * YAML property binding variations between Spring Cloud Gateway versions.
 */
@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 1. Authentication & OAuth2 Service (including Google OAuth2 authorization and callback endpoints)
                .route("auth-service", r -> r
                        .path("/api/v1/auth/**", "/oauth2/**", "/login/oauth2/**")
                        .uri("lb://examsy-auth-service"))

                // 2. User Profile Service (Students, Teachers, Admins)
                .route("profile-service", r -> r
                        .path("/api/v1/students/**", "/api/v1/teachers/**", "/api/v1/admins/**")
                        .uri("lb://examsy-profile-service"))

                // 3. Student Class Report Moderation Route (Higher precedence)
                .route("student-report-service", r -> r
                        .path("/api/v1/student/dashboard/classes/report")
                        .uri("lb://examsy-admin-service"))

                // 4. Class & Course Management Service
                .route("class-service", r -> r
                        .path("/api/v1/teacher/dashboard/classes/**", "/api/v1/teacher/classes/**", "/api/v1/student/dashboard/classes/**")
                        .uri("lb://examsy-class-service"))

                // 5. Grading & AI Service (Evaluations & Approvals)
                .route("grading-service", r -> r
                        .path("/api/v1/teacher/exams/*/grade/**", "/api/v1/teacher/exams/pending-gradings/**", "/api/v1/mock-exams/**")
                        .uri("lb://examsy-grading-service"))

                // 6. Exam & Proctoring Service
                .route("exam-service", r -> r
                        .path("/api/v1/teacher/exams/**", "/api/v1/student/exams/**", "/api/v1/teacher/dashboard/calendar/**", "/api/v1/student/dashboard/calendar/**")
                        .uri("lb://examsy-exam-service"))

                // 7. Notification Service
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .uri("lb://examsy-notification-service"))

                // 8. Admin Moderation & Reports Service
                .route("admin-service", r -> r
                        .path("/api/v1/admin/**")
                        .uri("lb://examsy-admin-service"))
                .build();
    }
}
