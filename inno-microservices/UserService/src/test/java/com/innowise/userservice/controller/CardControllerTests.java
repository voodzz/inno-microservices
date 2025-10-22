package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.CardDto;
import com.innowise.userservice.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CardService cardService;

  @Test
  @WithMockUser(roles = {"ADMIN"})
  @DisplayName("POST /api/v1/cards creates card and returns 201")
  void createCard_ShouldReturnCreated() throws Exception {
    CardDto created = new CardDto(1L, 99L, "1234", "Holder", LocalDate.now().plusYears(1));
    Mockito.when(cardService.create(any(CardDto.class))).thenReturn(created);

    String body =
        "{\n"
            + "  \"id\": null,\n"
            + "  \"userId\": 99,\n"
            + "  \"number\": \"1234\",\n"
            + "  \"holder\": \"Holder\",\n"
            + "  \"expirationDate\": \"2099-01-01\"\n"
            + "}";

    mockMvc
        .perform(post("/api/v1/cards").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  @DisplayName("GET /api/v1/cards/{id} returns card")
  void findById_ShouldReturnOk() throws Exception {
    CardDto dto = new CardDto(10L, 5L, "1111", "A", LocalDate.now().plusYears(2));
    Mockito.when(cardService.findById(10L)).thenReturn(dto);

    mockMvc
        .perform(get("/api/v1/cards/10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10));
  }

  @Test
  @WithMockUser(roles = {"ADMIN"})
  @DisplayName("GET /api/v1/cards with ids returns list, empty -> 204")
  void findAll_WithOrWithoutIds() throws Exception {
    Mockito.when(cardService.findAll()).thenReturn(List.of());
    mockMvc.perform(get("/api/v1/cards")).andExpect(status().isNoContent());

    List<CardDto> list =
        List.of(
            new CardDto(1L, 1L, "1", "H1", LocalDate.now().plusYears(1)),
            new CardDto(2L, 2L, "2", "H2", LocalDate.now().plusYears(1)));
    Mockito.when(cardService.findByIds(eq(List.of(1L, 2L)))).thenReturn(list);

    mockMvc
        .perform(get("/api/v1/cards").param("ids", "1,2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  @DisplayName("PUT /api/v1/cards/{id} returns 200")
  void updateCard_ShouldReturnOk() throws Exception {
    String body =
        "{\n"
            + "  \"id\": 7,\n"
            + "  \"userId\": 3,\n"
            + "  \"number\": \"9999\",\n"
            + "  \"holder\": \"H\",\n"
            + "  \"expirationDate\": \"2099-01-01\"\n"
            + "}";

    mockMvc
        .perform(put("/api/v1/cards/7").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());

    Mockito.verify(cardService).update(eq(7L), any(CardDto.class));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  @DisplayName("DELETE /api/v1/cards/{id} returns 204")
  void deleteCard_ShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/v1/cards/15")).andExpect(status().isNoContent());

    Mockito.verify(cardService).delete(15L);
  }
}
