package lk.ijse.examsy.classservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassStreamDTO {
    private String classCode;
    private String title;
    private String section;
    private String themeColorHex;
    private String bannerImageUrl;
    private List<AnnouncementDTO> announcements;
}
