package com.personalspace.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalspace.api.dto.request.CreateNoteRequest;
import com.personalspace.api.dto.request.UpdateNoteRequest;
import com.personalspace.api.dto.response.NoteResponse;
import com.personalspace.api.dto.response.PaginatedResponse;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.service.NoteService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = NoteController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.personalspace\\.api\\.security\\..*")
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal mockPrincipal = () -> "test@test.com";

    private NoteResponse createNoteResponse() {
        return new NoteResponse(
                UUID.randomUUID(), "Test Note", "Content",
                false, false, List.of(),
                Instant.now(), Instant.now()
        );
    }

    @Test
    void createNote_shouldReturn201() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest("Test Note", "Content", null, null);
        NoteResponse response = createNoteResponse();

        when(noteService.createNote(anyString(), any(CreateNoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notes")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Note"));
    }

    @Test
    void createNote_shouldReturn400_whenValidationFails() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest("", null, null, null);

        mockMvc.perform(post("/api/notes")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotes_shouldReturn200() throws Exception {
        NoteResponse noteResponse = createNoteResponse();
        PaginatedResponse<NoteResponse> response = new PaginatedResponse<>(
                List.of(noteResponse), 0, 10, 1, 1);

        when(noteService.getNotes(anyString(), eq(false), eq(0), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/notes").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Note"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getNote_shouldReturn200() throws Exception {
        UUID noteId = UUID.randomUUID();
        NoteResponse response = createNoteResponse();

        when(noteService.getNote(anyString(), eq(noteId))).thenReturn(response);

        mockMvc.perform(get("/api/notes/" + noteId).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"));
    }

    @Test
    void getNote_shouldReturn404_whenNotFound() throws Exception {
        UUID noteId = UUID.randomUUID();

        when(noteService.getNote(anyString(), eq(noteId)))
                .thenThrow(new ResourceNotFoundException("Note not found with id: " + noteId));

        mockMvc.perform(get("/api/notes/" + noteId).principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNote_shouldReturn200() throws Exception {
        UUID noteId = UUID.randomUUID();
        UpdateNoteRequest request = new UpdateNoteRequest("Updated Title", "Updated Content", true, false, null);
        NoteResponse response = new NoteResponse(
                noteId, "Updated Title", "Updated Content",
                true, false, List.of(),
                Instant.now(), Instant.now()
        );

        when(noteService.updateNote(anyString(), eq(noteId), any(UpdateNoteRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/notes/" + noteId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteNote_shouldReturn204() throws Exception {
        UUID noteId = UUID.randomUUID();

        doNothing().when(noteService).deleteNote(anyString(), eq(noteId));

        mockMvc.perform(delete("/api/notes/" + noteId).principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    void searchNotes_shouldReturn200() throws Exception {
        NoteResponse noteResponse = createNoteResponse();
        PaginatedResponse<NoteResponse> response = new PaginatedResponse<>(
                List.of(noteResponse), 0, 10, 1, 1);

        when(noteService.searchNotes(anyString(), eq("test"), eq(false), eq(0), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/notes/search").param("q", "test").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Note"));
    }

    @Test
    void togglePin_shouldReturn200() throws Exception {
        UUID noteId = UUID.randomUUID();
        NoteResponse response = new NoteResponse(
                noteId, "Test Note", "Content",
                true, false, List.of(),
                Instant.now(), Instant.now()
        );

        when(noteService.togglePin(anyString(), eq(noteId))).thenReturn(response);

        mockMvc.perform(patch("/api/notes/" + noteId + "/pin").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void toggleArchive_shouldReturn200() throws Exception {
        UUID noteId = UUID.randomUUID();
        NoteResponse response = new NoteResponse(
                noteId, "Test Note", "Content",
                false, true, List.of(),
                Instant.now(), Instant.now()
        );

        when(noteService.toggleArchive(anyString(), eq(noteId))).thenReturn(response);

        mockMvc.perform(patch("/api/notes/" + noteId + "/archive").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));
    }
}
