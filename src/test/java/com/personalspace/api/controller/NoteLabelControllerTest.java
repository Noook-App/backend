package com.personalspace.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalspace.api.dto.request.CreateNoteLabelRequest;
import com.personalspace.api.dto.request.UpdateNoteLabelRequest;
import com.personalspace.api.dto.response.NoteLabelResponse;
import com.personalspace.api.exception.DuplicateNoteLabelException;
import com.personalspace.api.service.NoteLabelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = NoteLabelController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.personalspace\\.api\\.security\\..*")
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NoteLabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteLabelService noteLabelService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal mockPrincipal = () -> "test@test.com";

    @Test
    void createLabel_shouldReturn201() throws Exception {
        CreateNoteLabelRequest request = new CreateNoteLabelRequest("Work");
        NoteLabelResponse response = new NoteLabelResponse(UUID.randomUUID(), "Work", Instant.now());

        when(noteLabelService.createLabel(anyString(), any(CreateNoteLabelRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notes/labels")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"));
    }

    @Test
    void createLabel_shouldReturn400_whenValidationFails() throws Exception {
        CreateNoteLabelRequest request = new CreateNoteLabelRequest("");

        mockMvc.perform(post("/api/notes/labels")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLabel_shouldReturn409_whenDuplicate() throws Exception {
        CreateNoteLabelRequest request = new CreateNoteLabelRequest("Work");

        when(noteLabelService.createLabel(anyString(), any(CreateNoteLabelRequest.class)))
                .thenThrow(new DuplicateNoteLabelException("Label already exists: Work"));

        mockMvc.perform(post("/api/notes/labels")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getLabels_shouldReturn200() throws Exception {
        NoteLabelResponse label = new NoteLabelResponse(UUID.randomUUID(), "Work", Instant.now());

        when(noteLabelService.getLabels(anyString())).thenReturn(List.of(label));

        mockMvc.perform(get("/api/notes/labels").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Work"));
    }

    @Test
    void updateLabel_shouldReturn200() throws Exception {
        UUID labelId = UUID.randomUUID();
        UpdateNoteLabelRequest request = new UpdateNoteLabelRequest("Updated");
        NoteLabelResponse response = new NoteLabelResponse(labelId, "Updated", Instant.now());

        when(noteLabelService.updateLabel(anyString(), eq(labelId), any(UpdateNoteLabelRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/notes/labels/" + labelId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteLabel_shouldReturn204() throws Exception {
        UUID labelId = UUID.randomUUID();

        doNothing().when(noteLabelService).deleteLabel(anyString(), eq(labelId));

        mockMvc.perform(delete("/api/notes/labels/" + labelId).principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }
}
