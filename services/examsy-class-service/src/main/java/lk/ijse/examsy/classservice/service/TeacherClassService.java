package lk.ijse.examsy.classservice.service;

import lk.ijse.examsy.classservice.dto.*;

import java.util.List;

public interface TeacherClassService {
    ClassStreamDTO getClassStream(Integer classId);
    AnnouncementDTO postAnnouncement(Integer classId, String username, CreateAnnouncementDTO dto);
    AnnouncementDTO updateAnnouncement(Integer classId, Integer announcementId, String username, CreateAnnouncementDTO dto);
    void deleteAnnouncement(Integer classId, Integer announcementId, String username);
    void updateClassAppearance(Integer classId, String username, UpdateAppearanceDTO dto);
    ClassPeopleDTO getClassPeople(Integer classId);
    void removeStudentFromClass(String teacherUsername, Integer classId, Integer studentId);
    void inviteStudent(String teacherUsername, Integer classId, InviteStudentDTO dto);
    List<JoinRequestDTO> getPendingJoinRequests(String teacherUsername, Integer classId);
    void approveJoinRequest(String teacherUsername, Integer requestId);
    void rejectJoinRequest(String teacherUsername, Integer requestId);
}
