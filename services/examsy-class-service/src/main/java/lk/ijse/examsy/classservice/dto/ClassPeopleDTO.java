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
public class ClassPeopleDTO {
    private List<PersonDTO> teachers;
    private List<PersonDTO> students;
}
