package com.personalspace.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalspace.api.dto.request.CreateGroceryLabelRequest;
import com.personalspace.api.dto.request.UpdateGroceryLabelRequest;
import com.personalspace.api.dto.response.GroceryLabelResponse;
import com.personalspace.api.exception.DuplicateGroceryLabelException;
import com.personalspace.api.service.GroceryLabelService;
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
        controllers = GroceryLabelController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.personalspace\\.api\\.security\\..*")
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GroceryLabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroceryLabelService groceryLabelService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal mockPrincipal = () -> "test@test.com";

    @Test
    void createLabel_shouldReturn201() throws Exception {
        CreateGroceryLabelRequest request = new CreateGroceryLabelRequest("Produce");
        GroceryLabelResponse response = new GroceryLabelResponse(UUID.randomUUID(), "Produce", Instant.now());

        when(groceryLabelService.createLabel(anyString(), any(CreateGroceryLabelRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/grocery-lists/labels")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Produce"));
    }

    @Test
    void createLabel_shouldReturn400_whenValidationFails() throws Exception {
        CreateGroceryLabelRequest request = new CreateGroceryLabelRequest("");

        mockMvc.perform(post("/api/grocery-lists/labels")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLabel_shouldReturn409_whenDuplicate() throws Exception {
        CreateGroceryLabelRequest request = new CreateGroceryLabelRequest("Produce");

        when(groceryLabelService.createLabel(anyString(), any(CreateGroceryLabelRequest.class)))
                .thenThrow(new DuplicateGroceryLabelException("Label already exists: Produce"));

        mockMvc.perform(post("/api/grocery-lists/labels")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getLabels_shouldReturn200() throws Exception {
        GroceryLabelResponse label = new GroceryLabelResponse(UUID.randomUUID(), "Produce", Instant.now());

        when(groceryLabelService.getLabels(anyString())).thenReturn(List.of(label));

        mockMvc.perform(get("/api/grocery-lists/labels").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Produce"));
    }

    @Test
    void updateLabel_shouldReturn200() throws Exception {
        UUID labelId = UUID.randomUUID();
        UpdateGroceryLabelRequest request = new UpdateGroceryLabelRequest("Updated");
        GroceryLabelResponse response = new GroceryLabelResponse(labelId, "Updated", Instant.now());

        when(groceryLabelService.updateLabel(anyString(), eq(labelId), any(UpdateGroceryLabelRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/grocery-lists/labels/" + labelId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteLabel_shouldReturn204() throws Exception {
        UUID labelId = UUID.randomUUID();

        doNothing().when(groceryLabelService).deleteLabel(anyString(), eq(labelId));

        mockMvc.perform(delete("/api/grocery-lists/labels/" + labelId).principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }
}
