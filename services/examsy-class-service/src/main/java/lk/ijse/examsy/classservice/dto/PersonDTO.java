package lk.ijse.examsy.classservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDTO {
    private Integer id;
    private String name;
    private String email;
    private String initial;
    private String role;
    private String profileImageUrl;
}
