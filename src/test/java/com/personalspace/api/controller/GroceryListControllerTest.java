package com.personalspace.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalspace.api.dto.request.CreateGroceryListRequest;
import com.personalspace.api.dto.request.UpdateGroceryListRequest;
import com.personalspace.api.dto.response.GroceryListResponse;
import com.personalspace.api.dto.response.PaginatedResponse;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.service.GroceryListService;
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
        controllers = GroceryListController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.personalspace\\.api\\.security\\..*")
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GroceryListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroceryListService groceryListService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal mockPrincipal = () -> "test@test.com";

    private GroceryListResponse createGroceryListResponse() {
        return new GroceryListResponse(
                UUID.randomUUID(), "Weekly Groceries", false,
                List.of(), List.of(),
                Instant.now(), Instant.now()
        );
    }

    @Test
    void createGroceryList_shouldReturn201() throws Exception {
        CreateGroceryListRequest request = new CreateGroceryListRequest("Weekly Groceries", null, null);
        GroceryListResponse response = createGroceryListResponse();

        when(groceryListService.createGroceryList(anyString(), any(CreateGroceryListRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/grocery-lists")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Weekly Groceries"));
    }

    @Test
    void createGroceryList_shouldReturn400_whenValidationFails() throws Exception {
        CreateGroceryListRequest request = new CreateGroceryListRequest("", null, null);

        mockMvc.perform(post("/api/grocery-lists")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGroceryLists_shouldReturn200() throws Exception {
        GroceryListResponse listResponse = createGroceryListResponse();
        PaginatedResponse<GroceryListResponse> response = new PaginatedResponse<>(
                List.of(listResponse), 0, 10, 1, 1);

        when(groceryListService.getGroceryLists(anyString(), eq(false), eq(0), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/grocery-lists").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Weekly Groceries"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getGroceryList_shouldReturn200() throws Exception {
        UUID listId = UUID.randomUUID();
        GroceryListResponse response = createGroceryListResponse();

        when(groceryListService.getGroceryList(anyString(), eq(listId))).thenReturn(response);

        mockMvc.perform(get("/api/grocery-lists/" + listId).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Weekly Groceries"));
    }

    @Test
    void getGroceryList_shouldReturn404_whenNotFound() throws Exception {
        UUID listId = UUID.randomUUID();

        when(groceryListService.getGroceryList(anyString(), eq(listId)))
                .thenThrow(new ResourceNotFoundException("Grocery list not found with id: " + listId));

        mockMvc.perform(get("/api/grocery-lists/" + listId).principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateGroceryList_shouldReturn200() throws Exception {
        UUID listId = UUID.randomUUID();
        UpdateGroceryListRequest request = new UpdateGroceryListRequest("Updated Title", null);
        GroceryListResponse response = new GroceryListResponse(
                listId, "Updated Title", false,
                List.of(), List.of(),
                Instant.now(), Instant.now()
        );

        when(groceryListService.updateGroceryList(anyString(), eq(listId), any(UpdateGroceryListRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/grocery-lists/" + listId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void deleteGroceryList_shouldReturn204() throws Exception {
        UUID listId = UUID.randomUUID();

        doNothing().when(groceryListService).deleteGroceryList(anyString(), eq(listId));

        mockMvc.perform(delete("/api/grocery-lists/" + listId).principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    void searchGroceryLists_shouldReturn200() throws Exception {
        GroceryListResponse listResponse = createGroceryListResponse();
        PaginatedResponse<GroceryListResponse> response = new PaginatedResponse<>(
                List.of(listResponse), 0, 10, 1, 1);

        when(groceryListService.searchGroceryLists(anyString(), eq("Weekly"), eq(false), eq(0), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/grocery-lists/search")
                        .principal(mockPrincipal)
                        .param("q", "Weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Weekly Groceries"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void toggleArchive_shouldReturn200() throws Exception {
        UUID listId = UUID.randomUUID();
        GroceryListResponse response = new GroceryListResponse(
                listId, "Weekly Groceries", true,
                List.of(), List.of(),
                Instant.now(), Instant.now()
        );

        when(groceryListService.toggleArchive(anyString(), eq(listId))).thenReturn(response);

        mockMvc.perform(patch("/api/grocery-lists/" + listId + "/archive").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true));
    }
}
