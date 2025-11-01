package com.innowise.userservice.integration;

import com.innowise.userservice.exception.AlreadyExistsException;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class UserServiceIntegrationTests extends IntegrationTestBase {
  private static final String USER_EMAIL = "test@example.com";
  private static final String CACHE_NAME = "USER_CACHE";

  @Autowired private UserService userService;

  @Autowired private UserRepository userRepository;

  @Autowired private CacheManager cacheManager;

  @BeforeEach
  void setup() {
    userRepository.deleteAll();
  }

  @Test
  void create_ShouldPersistUserAndPutToCache() {
    UserDto request =
        new UserDto(1L, "John", "Doe", LocalDate.of(1990, 1, 1), USER_EMAIL, List.of());

    UserDto created = userService.create(request);

    assertNotNull(created);
    assertNotNull(created.id());

    Optional<User> maybe = userRepository.findById(created.id());
    assertTrue(maybe.isPresent());
    assertEquals(USER_EMAIL, maybe.get().getEmail());

    Cache cache = cacheManager.getCache(CACHE_NAME);
    assertNotNull(cache);
    Object cachedById = cache.get(created.id(), Object.class);
    Object cachedByEmail = cache.get(created.email(), Object.class);
    assertNotNull(cachedById);
    assertNotNull(cachedByEmail);
  }

  @Test
  void create_DuplicateEmail_ShouldThrowAlreadyExists() {
    User existing = new User();
    existing.setId(1L);
    existing.setName("Existing");
    existing.setSurname("User");
    existing.setEmail(USER_EMAIL);
    existing.setBirthDate(LocalDate.of(1985, 5, 5));
    userRepository.save(existing);

    UserDto request =
        new UserDto(2L, "John", "Doe", LocalDate.of(1990, 1, 1), USER_EMAIL, List.of());

    assertThrows(AlreadyExistsException.class, () -> userService.create(request));
  }

  @Test
  void findById_ShouldReturnUser() {
    User user = new User();
    user.setId(4L);
    user.setName("Alice");
    user.setSurname("Cooper");
    user.setEmail("alice@example.com");
    user.setBirthDate(LocalDate.of(1992, 2, 2));
    User saved = userRepository.save(user);

    UserDto dto = userService.findById(saved.getId());
    assertNotNull(dto);
    assertEquals(saved.getId(), dto.id());
    assertEquals("alice@example.com", dto.email());
  }

  @Test
  void findById_Cacheable_ShouldPutToCache() {
    User user = new User();
    user.setId(1L);
    user.setName("Cachey");
    user.setSurname("McCache");
    user.setEmail("cachey@example.com");
    user.setBirthDate(LocalDate.of(1993, 3, 3));
    User saved = userRepository.save(user);

    Cache cache = cacheManager.getCache(CACHE_NAME);
    assertNotNull(cache);

    UserDto dto1 = userService.findById(saved.getId());
    assertNotNull(dto1);

    Object cached = cache.get(saved.getId(), Object.class);
    assertNotNull(cached);

    UserDto dto2 = userService.findById(saved.getId());
    assertNotNull(dto2);
    assertEquals(dto1.id(), dto2.id());
  }

  @Test
  void findByEmail_ShouldReturnUserAndCacheByEmail() {
    User user = new User();
    user.setId(1L);
    user.setName("Email");
    user.setSurname("Finder");
    user.setEmail("finder@example.com");
    user.setBirthDate(LocalDate.of(1994, 4, 4));
    User saved = userRepository.save(user);

    Cache cache = cacheManager.getCache(CACHE_NAME);
    assertNotNull(cache);

    UserDto dto = userService.findByEmail(saved.getEmail());
    assertNotNull(dto);
    assertEquals(saved.getEmail(), dto.email());

    Object cachedByEmail = cache.get(saved.getEmail(), Object.class);
    assertNotNull(cachedByEmail);
  }

  @Test
  void findByIds_ShouldReturnMultiple() {
    User u1 = new User();
    u1.setId(1L);
    u1.setName("One");
    u1.setSurname("One");
    u1.setEmail("one@example.com");
    u1.setBirthDate(LocalDate.of(1980, 1, 1));

    User u2 = new User();
    u2.setId(2L);
    u2.setName("Two");
    u2.setSurname("Two");
    u2.setEmail("two@example.com");
    u2.setBirthDate(LocalDate.of(1981, 2, 2));

    User s1 = userRepository.save(u1);
    User s2 = userRepository.save(u2);

    List<UserDto> dtos = userService.findByIds(List.of(s1.getId(), s2.getId()));
    assertNotNull(dtos);
    assertEquals(2, dtos.size());
    Set<Long> ids = dtos.stream().map(UserDto::id).collect(Collectors.toSet());
    assertTrue(ids.contains(s1.getId()));
    assertTrue(ids.contains(s2.getId()));
  }

  @Test
  void findAll_ShouldReturnAll() {
      User user1 = new User();
      user1.setId(1L);
      user1.setName("A");
      user1.setSurname("A");
      user1.setEmail("a@example.com");
      user1.setBirthDate(LocalDate.of(1970, 1, 1));

      User user2 = new User();
      user2.setId(2L);
      user2.setName("B");
      user2.setSurname("B");
      user2.setEmail("b@example.com");
      user2.setBirthDate(LocalDate.of(1970, 2, 2));

      List<User> toSave = List.of(user1, user2);
    userRepository.saveAll(toSave);

    List<UserDto> all = userService.findAll();
    assertNotNull(all);
    assertTrue(all.size() >= 2);
  }

  @Test
  void update_ShouldUpdateUserAndUpdateCache() {
    User user = new User();
    user.setId(1L);
    user.setName("Before");
    user.setSurname("Update");
    user.setEmail("upd@example.com");
    user.setBirthDate(LocalDate.of(1977, 7, 7));
    User saved = userRepository.save(user);

    Cache cache = cacheManager.getCache(CACHE_NAME);
    assertNotNull(cache);

    UserDto request =
        new UserDto(
            saved.getId(),
            "After",
            "Updated",
            LocalDate.of(1978, 8, 8),
            saved.getEmail(),
            List.of());

    userService.update(saved.getId(), request);

    User updated = userRepository.findById(saved.getId()).orElseThrow();
    assertEquals("After", updated.getName());
    assertEquals("Updated", updated.getSurname());
    assertEquals(LocalDate.of(1978, 8, 8), updated.getBirthDate());

    var cachedById = cache.get(saved.getId(), UserDto.class);
    assertNotNull(cachedById);
    assertEquals("After", cachedById.name());

    var cachedByEmail = cache.get(saved.getEmail(), UserDto.class);
    assertNotNull(cachedByEmail);
    assertEquals("After", cachedByEmail.name());
  }

  @Test
  void update_NonExistent_ShouldThrowNotFound() {
    UserDto req =
        new UserDto(1L, "No", "User", LocalDate.of(2000, 1, 1), "no@example.com", List.of());
    assertThrows(NotFoundException.class, () -> userService.update(9999L, req));
  }

  @Test
  void delete_ShouldRemoveUserAndEvictCache() {
    User user = new User();
    user.setId(1L);
    user.setName("ToDelete");
    user.setSurname("User");
    user.setEmail("todel@example.com");
    user.setBirthDate(LocalDate.of(1980, 8, 8));
    User saved = userRepository.save(user);

    Cache cache = cacheManager.getCache(CACHE_NAME);
    assertNotNull(cache);
    UserDto dtoForCache =
        new UserDto(
            saved.getId(),
            saved.getName(),
            saved.getSurname(),
            saved.getBirthDate(),
            saved.getEmail(),
            List.of());
    cache.put(saved.getId(), dtoForCache);
    cache.put(saved.getEmail(), dtoForCache);

    userService.delete(saved.getId());

    assertFalse(userRepository.findById(saved.getId()).isPresent());
    assertNull(cache.get(saved.getId()));
    assertNull(cache.get(saved.getEmail()));
  }

  @Test
  void delete_NonExistent_ShouldThrowNotFound() {
    assertThrows(NotFoundException.class, () -> userService.delete(9999L));
  }
}
