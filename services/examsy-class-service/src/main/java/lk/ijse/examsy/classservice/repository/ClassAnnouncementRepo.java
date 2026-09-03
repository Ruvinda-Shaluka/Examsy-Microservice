package lk.ijse.examsy.classservice.repository;

import lk.ijse.examsy.classservice.entity.ClassAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassAnnouncementRepo extends JpaRepository<ClassAnnouncement, Integer> {
    List<ClassAnnouncement> findByCourseIdOrderByCreatedAtDesc(Integer courseId);
}
