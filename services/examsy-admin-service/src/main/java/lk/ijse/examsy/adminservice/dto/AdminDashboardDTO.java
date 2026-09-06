package lk.ijse.examsy.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private long totalStudents;
    private long activeTeachers;
    private long pendingReports;
    private long totalUsers;
    private List<ReportDistributionDTO> reportDistribution;
}
