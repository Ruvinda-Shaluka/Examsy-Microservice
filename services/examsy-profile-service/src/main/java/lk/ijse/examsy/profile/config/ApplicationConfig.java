package lk.ijse.examsy.profile.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.ijse.examsy.profile.dto.AdminProfileDTO;
import lk.ijse.examsy.profile.dto.StudentDTO;
import lk.ijse.examsy.profile.dto.TeacherDTO;
import lk.ijse.examsy.profile.entity.Admin;
import lk.ijse.examsy.profile.entity.Student;
import lk.ijse.examsy.profile.entity.Teacher;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Map userId to userAccountId in StudentDTO
        modelMapper.typeMap(Student.class, StudentDTO.class).addMappings(mapper -> {
            mapper.map(Student::getUserId, StudentDTO::setUserAccountId);
        });

        // Map userId to userAccountId in TeacherDTO
        modelMapper.typeMap(Teacher.class, TeacherDTO.class).addMappings(mapper -> {
            mapper.map(Teacher::getUserId, TeacherDTO::setUserAccountId);
        });

        // Map userId to userAccountId in AdminProfileDTO
        modelMapper.typeMap(Admin.class, AdminProfileDTO.class).addMappings(mapper -> {
            mapper.map(Admin::getUserId, AdminProfileDTO::setUserAccountId);
        });

        return modelMapper;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
