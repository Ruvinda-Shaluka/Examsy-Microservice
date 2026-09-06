package lk.ijse.examsy.adminservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "metric_key", nullable = false, unique = true, length = 100)
    private String metricKey;

    @Builder.Default
    @Column(name = "metric_value", nullable = false)
    private Long metricValue = 0L;

    @Column(length = 255)
    private String description;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
