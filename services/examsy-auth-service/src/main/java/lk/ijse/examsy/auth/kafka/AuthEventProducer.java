package lk.ijse.examsy.auth.kafka;

import lk.ijse.examsy.auth.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {

    public static final String USER_REGISTERED_TOPIC = "examsy.user.registered";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publishing user.registered event to Kafka topic [{}]: username={}, role={}",
                USER_REGISTERED_TOPIC, event.getUsername(), event.getRole());
        kafkaTemplate.send(USER_REGISTERED_TOPIC, event.getUsername(), event);
    }
}
