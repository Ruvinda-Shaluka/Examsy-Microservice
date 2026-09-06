package lk.ijse.examsy.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfileDTO {
    private Integer id;
    private Integer userAccountId;
    private String username;
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private String roleLevel;
}
