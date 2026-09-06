package lk.ijse.examsy.adminservice.repository;

import lk.ijse.examsy.adminservice.dto.ReportDistributionDTO;
import lk.ijse.examsy.adminservice.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepo extends JpaRepository<Report, Integer> {

    List<Report> findByStatusOrderByReportedAtDesc(String status);

    long countByStatus(String status);

    long countByTargetTeacherUsername(String targetTeacherUsername);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.targetTeacherUsername = :teacherUsername OR r.targetTeacherName = :teacherName")
    long countComplaintsByTeacher(@Param("teacherUsername") String teacherUsername, @Param("teacherName") String teacherName);

    @Query("SELECT new lk.ijse.examsy.adminservice.dto.ReportDistributionDTO(r.category, COUNT(r)) FROM Report r GROUP BY r.category")
    List<ReportDistributionDTO> countReportsByCategory();
}
