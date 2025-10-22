package com.innowise.userservice.controller;

import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.service.UserService;
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

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @Test
  @WithMockUser(roles = {"ADMIN"})
  @DisplayName("POST /api/v1/users creates user and returns 201")
  void createUser_ShouldReturnCreated() throws Exception {
    UserDto created =
        new UserDto(1L, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com", List.of());
    Mockito.when(userService.create(any(UserDto.class))).thenReturn(created);

    String body =
        "{\n"
            + "  \"id\": null,\n"
            + "  \"name\": \"John\",\n"
            + "  \"surname\": \"Doe\",\n"
            + "  \"birthDate\": \"1990-01-01\",\n"
            + "  \"email\": \"john@example.com\",\n"
            + "  \"cards\": []\n"
            + "}";

    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  @DisplayName("GET /api/v1/users/{id} returns user")
  void findById_ShouldReturnOk() throws Exception {
    UserDto dto =
        new UserDto(
            2L, "Alice", "Cooper", LocalDate.of(1992, 2, 2), "alice@example.com", List.of());
    Mockito.when(userService.findById(2L)).thenReturn(dto);

    mockMvc
        .perform(get("/api/v1/users/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.email").value("alice@example.com"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "SERVICE"})
  @DisplayName("GET /api/v1/users without filter returns 204 when empty")
  void findBy_NoFilter_Empty_ShouldReturnNoContent() throws Exception {
    Mockito.when(userService.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/users")).andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "SERVICE"})
  @DisplayName("GET /api/v1/users?filter=ids returns list")
  void findBy_FilterIds_ShouldReturnList() throws Exception {
    List<UserDto> list =
        List.of(
            new UserDto(1L, "A", "A", LocalDate.of(1990, 1, 1), "a@example.com", List.of()),
            new UserDto(2L, "B", "B", LocalDate.of(1991, 2, 2), "b@example.com", List.of()));
    Mockito.when(userService.findByIds(eq(List.of(1L, 2L)))).thenReturn(list);

    mockMvc
        .perform(get("/api/v1/users").param("filter", "ids").param("ids", "1,2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "SERVICE"})
  @DisplayName("GET /api/v1/users?filter=email returns single element list")
  void findBy_FilterEmail_ShouldReturnSingle() throws Exception {
    UserDto dto = new UserDto(5L, "E", "E", LocalDate.of(1993, 3, 3), "e@example.com", List.of());
    Mockito.when(userService.findByEmail("e@example.com")).thenReturn(dto);

    mockMvc
        .perform(get("/api/v1/users").param("filter", "email").param("email", "e@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].email").value("e@example.com"));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  @DisplayName("PUT /api/v1/users/{id} returns 200 on update")
  void updateUser_ShouldReturnOk() throws Exception {
    String body =
        "{\n"
            + "  \"id\": 10,\n"
            + "  \"name\": \"New\",\n"
            + "  \"surname\": \"Name\",\n"
            + "  \"birthDate\": \"2000-01-01\",\n"
            + "  \"email\": \"new@example.com\",\n"
            + "  \"cards\": []\n"
            + "}";

    mockMvc
        .perform(put("/api/v1/users/10").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());

    Mockito.verify(userService).update(eq(10L), any(UserDto.class));
  }

  @Test
  @WithMockUser(roles = {"ADMIN", "USER"})
  @DisplayName("DELETE /api/v1/users/{id} returns 204")
  void deleteUser_ShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/v1/users/77")).andExpect(status().isNoContent());

    Mockito.verify(userService).delete(77L);
  }
}
