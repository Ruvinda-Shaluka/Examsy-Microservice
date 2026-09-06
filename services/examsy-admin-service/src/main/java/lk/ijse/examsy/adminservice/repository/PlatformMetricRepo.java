package lk.ijse.examsy.adminservice.repository;

import lk.ijse.examsy.adminservice.entity.PlatformMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformMetricRepo extends JpaRepository<PlatformMetric, Integer> {

    Optional<PlatformMetric> findByMetricKey(String metricKey);

    @Modifying
    @Query("UPDATE PlatformMetric m SET m.metricValue = m.metricValue + :delta WHERE m.metricKey = :key")
    int incrementMetric(@Param("key") String key, @Param("delta") long delta);

    @Modifying
    @Query("UPDATE PlatformMetric m SET m.metricValue = :val WHERE m.metricKey = :key")
    int setMetric(@Param("key") String key, @Param("val") long val);
}
