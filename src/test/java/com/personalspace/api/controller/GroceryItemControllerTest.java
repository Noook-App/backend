package com.personalspace.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalspace.api.dto.request.CreateGroceryItemRequest;
import com.personalspace.api.dto.request.UpdateGroceryItemRequest;
import com.personalspace.api.dto.response.GroceryItemResponse;
import com.personalspace.api.exception.ResourceNotFoundException;
import com.personalspace.api.service.GroceryItemService;
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
        controllers = GroceryItemController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                ServletWebSecurityAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.personalspace\\.api\\.security\\..*")
)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GroceryItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroceryItemService groceryItemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Principal mockPrincipal = () -> "test@test.com";

    private final UUID listId = UUID.randomUUID();

    private GroceryItemResponse createGroceryItemResponse() {
        return new GroceryItemResponse(
                UUID.randomUUID(), "Apples", "5", false,
                List.of(), Instant.now(), Instant.now()
        );
    }

    @Test
    void createItem_shouldReturn201() throws Exception {
        CreateGroceryItemRequest request = new CreateGroceryItemRequest("Apples", "5", null);
        GroceryItemResponse response = createGroceryItemResponse();

        when(groceryItemService.createItem(anyString(), eq(listId), any(CreateGroceryItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/grocery-lists/" + listId + "/items")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apples"));
    }

    @Test
    void createItem_shouldReturn400_whenValidationFails() throws Exception {
        CreateGroceryItemRequest request = new CreateGroceryItemRequest("", null, null);

        mockMvc.perform(post("/api/grocery-lists/" + listId + "/items")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItems_shouldReturn200() throws Exception {
        GroceryItemResponse itemResponse = createGroceryItemResponse();

        when(groceryItemService.getItems(anyString(), eq(listId))).thenReturn(List.of(itemResponse));

        mockMvc.perform(get("/api/grocery-lists/" + listId + "/items").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apples"));
    }

    @Test
    void updateItem_shouldReturn200() throws Exception {
        UUID itemId = UUID.randomUUID();
        UpdateGroceryItemRequest request = new UpdateGroceryItemRequest("Updated Apples", "10", null);
        GroceryItemResponse response = new GroceryItemResponse(
                itemId, "Updated Apples", "10", false,
                List.of(), Instant.now(), Instant.now()
        );

        when(groceryItemService.updateItem(anyString(), eq(listId), eq(itemId), any(UpdateGroceryItemRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/grocery-lists/" + listId + "/items/" + itemId)
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Apples"));
    }

    @Test
    void deleteItem_shouldReturn204() throws Exception {
        UUID itemId = UUID.randomUUID();

        doNothing().when(groceryItemService).deleteItem(anyString(), eq(listId), eq(itemId));

        mockMvc.perform(delete("/api/grocery-lists/" + listId + "/items/" + itemId).principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    void toggleChecked_shouldReturn200() throws Exception {
        UUID itemId = UUID.randomUUID();
        GroceryItemResponse response = new GroceryItemResponse(
                itemId, "Apples", "5", true,
                List.of(), Instant.now(), Instant.now()
        );

        when(groceryItemService.toggleChecked(anyString(), eq(listId), eq(itemId))).thenReturn(response);

        mockMvc.perform(patch("/api/grocery-lists/" + listId + "/items/" + itemId + "/check").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checked").value(true));
    }

    @Test
    void getItems_shouldReturn404_whenListNotFound() throws Exception {
        when(groceryItemService.getItems(anyString(), eq(listId)))
                .thenThrow(new ResourceNotFoundException("Grocery list not found with id: " + listId));

        mockMvc.perform(get("/api/grocery-lists/" + listId + "/items").principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }
}
