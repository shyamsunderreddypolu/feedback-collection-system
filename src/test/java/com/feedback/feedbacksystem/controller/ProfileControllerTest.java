package com.feedback.feedbacksystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feedback.feedbacksystem.dto.FacultyProfileDto;
import com.feedback.feedbacksystem.dto.StudentProfileDto;
import com.feedback.feedbacksystem.security.jwt.JwtTokenProvider;
import com.feedback.feedbacksystem.security.service.CustomUserDetailsService;
import com.feedback.feedbacksystem.service.ProfileService;
import com.feedback.feedbacksystem.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getStudentProfileReturnsProfile() throws Exception {
        StudentProfileDto dto = StudentProfileDto.builder()
                .id(1L)
                .userId(10L)
                .rollNumber("21CS001")
                .year(3)
                .semester(5)
                .section("A")
                .batch("2021-2025")
                .build();

        when(profileService.getStudentProfileByUserId(10L)).thenReturn(dto);

        mockMvc.perform(get("/api/profiles/student/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value("21CS001"))
                .andExpect(jsonPath("$.semester").value(5));
    }

    @Test
    void updateStudentProfileReturnsUpdated() throws Exception {
        StudentProfileDto dto = StudentProfileDto.builder()
                .rollNumber("21CS001")
                .year(3)
                .semester(6)
                .section("B")
                .batch("2021-2025")
                .build();

        StudentProfileDto updated = StudentProfileDto.builder()
                .id(1L)
                .userId(10L)
                .rollNumber("21CS001")
                .year(3)
                .semester(6)
                .section("B")
                .batch("2021-2025")
                .build();

        when(profileService.updateStudentProfile(eq(10L), any(StudentProfileDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/profiles/student/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.semester").value(6))
                .andExpect(jsonPath("$.section").value("B"));
    }

    @Test
    void getFacultyProfileReturnsProfile() throws Exception {
        FacultyProfileDto dto = FacultyProfileDto.builder()
                .id(2L)
                .userId(20L)
                .employeeId("FAC001")
                .designation("Associate Professor")
                .build();

        when(profileService.getFacultyProfileByUserId(20L)).thenReturn(dto);

        mockMvc.perform(get("/api/profiles/faculty/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("FAC001"))
                .andExpect(jsonPath("$.designation").value("Associate Professor"));
    }
}
