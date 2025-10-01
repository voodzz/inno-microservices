package com.innowise.userservice.service;

import com.innowise.userservice.exception.AlreadyExistsException;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.exception.UpdateException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTests {
  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private CacheManager cacheManager;
  @Mock private Cache cache;

  @InjectMocks private UserServiceImpl userService;

  private UserDto userDto;
  private User user;
  private final Long USER_ID = 1L;
  private final String USER_EMAIL = "test@example.com";

  @BeforeEach
  void setUp() {
    when(cacheManager.getCache("USER_CACHE")).thenReturn(cache);

    userService.init();

    userDto =
        new UserDto(
            USER_ID, "John", "Doe", LocalDate.of(1990, 1, 1), USER_EMAIL, Collections.emptyList());

    user = new User();
    user.setId(USER_ID);
    user.setName("John");
    user.setSurname("Doe");
    user.setBirthDate(LocalDate.of(1990, 1, 1));
    user.setEmail(USER_EMAIL);
    user.setCards(Collections.emptyList());
  }

  @Test
  void create_ShouldReturnUserDto_WhenUserIsNew() {
    when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(false);
    when(userMapper.toEntity(any(UserDto.class))).thenReturn(user);
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(userMapper.toDto(any(User.class))).thenReturn(userDto);

    UserDto result = userService.create(userDto);

    assertNotNull(result);
    assertEquals(userDto.email(), result.email());
    verify(userRepository).existsByEmail(USER_EMAIL);
    verify(userRepository).save(user);
    verify(cache, times(1)).put(userDto.id(), userDto);
    verify(cache, times(1)).put(userDto.email(), userDto);
  }

  @Test
  void create_ShouldThrowAlreadyExistsException_WhenUserEmailExists() {
    when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

    assertThrows(AlreadyExistsException.class, () -> userService.create(userDto));

    verify(userRepository).existsByEmail(USER_EMAIL);
    verify(userRepository, never()).save(any(User.class));
    verify(cache, never()).put(any(), any());
  }

  @Test
  void findById_ShouldReturnUserDto_WhenUserExists() {
    when(userRepository.findByIdWithCards(USER_ID)).thenReturn(Optional.of(user));
    when(userMapper.toDto(any(User.class))).thenReturn(userDto);

    UserDto result = userService.findById(USER_ID);

    assertNotNull(result);
    assertEquals(USER_ID, result.id());
    verify(userRepository).findByIdWithCards(USER_ID);
  }

  @Test
  void findById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.findByIdWithCards(USER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.findById(USER_ID));

    verify(userRepository).findByIdWithCards(USER_ID);
    verify(userMapper, never()).toDto(any(User.class));
  }

  @Test
  void update_ShouldUpdateUserAndCache_WhenUserExistsAndUpdated() {
    UserDto updateRequest =
        new UserDto(USER_ID, "NewName", "NewSurname", LocalDate.of(1995, 5, 5), USER_EMAIL, null);

    User updatedUser = new User();
    updatedUser.setId(USER_ID);
    updatedUser.setName("NewName");
    updatedUser.setSurname("NewSurname");
    updatedUser.setBirthDate(LocalDate.of(1995, 5, 5));
    updatedUser.setEmail(USER_EMAIL);

    UserDto updatedDto =
        new UserDto(
            USER_ID,
            "NewName",
            "NewSurname",
            LocalDate.of(1995, 5, 5),
            USER_EMAIL,
            Collections.emptyList());

    when(userRepository.findById(USER_ID))
        .thenReturn(Optional.of(user))
        .thenReturn(Optional.of(updatedUser));

    when(userRepository.updateById(eq(USER_ID), anyString(), anyString(), any(LocalDate.class)))
        .thenReturn(1);
    when(userMapper.toDto(updatedUser)).thenReturn(updatedDto);

    userService.update(USER_ID, updateRequest);

    verify(userRepository)
        .updateById(eq(USER_ID), eq("NewName"), eq("NewSurname"), eq(LocalDate.of(1995, 5, 5)));

    verify(userRepository, times(2)).findById(USER_ID);

    verify(cache, times(1)).put(updatedDto.id(), updatedDto);
    verify(cache, times(1)).put(updatedDto.email(), updatedDto);
  }

  @Test
  void update_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.update(USER_ID, userDto));

    verify(userRepository).findById(USER_ID);
    verify(userRepository, never()).updateById(any(), any(), any(), any());
  }

  @Test
  void update_ShouldThrowUpdateException_WhenNoRowsUpdated() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.updateById(eq(USER_ID), anyString(), anyString(), any(LocalDate.class)))
        .thenReturn(0);

    assertThrows(UpdateException.class, () -> userService.update(USER_ID, userDto));

    verify(userRepository, times(1)).findById(USER_ID);

    verify(userRepository, times(1))
        .updateById(eq(USER_ID), anyString(), anyString(), any(LocalDate.class));

    verify(cache, never()).put(any(), any());
  }

  @Test
  void delete_ShouldDeleteUserAndEvictCache_WhenUserExists() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    doNothing().when(userRepository).delete(user);

    userService.delete(USER_ID);

    verify(userRepository).findById(USER_ID);
    verify(cache).evict(USER_ID);
    verify(cache).evict(USER_EMAIL);
    verify(userRepository).delete(user);
  }

  @Test
  void delete_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.delete(USER_ID));

    verify(userRepository).findById(USER_ID);
    verify(userRepository, never()).delete(any(User.class));
    verify(cache, never()).evict(any());
  }

  @Test
  void findByEmail_ShouldReturnUserDto_WhenUserExists() {
    when(userRepository.findByEmailWithCards(USER_EMAIL)).thenReturn(Optional.of(user));
    when(userMapper.toDto(any(User.class))).thenReturn(userDto);

    UserDto result = userService.findByEmail(USER_EMAIL);

    assertNotNull(result);
    assertEquals(USER_EMAIL, result.email());
    verify(userRepository).findByEmailWithCards(USER_EMAIL);
  }

  @Test
  void findByEmail_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
    when(userRepository.findByEmailWithCards(USER_EMAIL)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.findByEmail(USER_EMAIL));

    verify(userRepository).findByEmailWithCards(USER_EMAIL);
    verify(userMapper, never()).toDto(any(User.class));
  }

  @Test
  void findByIds_ShouldReturnListOfUserDtos_WhenUsersExist() {
    List<Long> userIds = List.of(USER_ID, 2L);
    User user2 = new User();
    user2.setId(2L);
    UserDto userDto2 =
        new UserDto(
            2L,
            "Jane",
            "Smith",
            LocalDate.of(1991, 2, 2),
            "jane@example.com",
            Collections.emptyList());

    List<User> userList = List.of(user, user2);
    List<UserDto> dtoList = List.of(userDto, userDto2);

    when(userRepository.findByIdIn(userIds)).thenReturn(userList);
    when(userMapper.toDtoList(userList)).thenReturn(dtoList);

    List<UserDto> result = userService.findByIds(userIds);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(USER_ID, result.getFirst().id());
    verify(userRepository).findByIdIn(userIds);
    verify(userMapper).toDtoList(userList);
  }

  @Test
  void findByIds_ShouldReturnEmptyList_WhenNoUsersFound() {
    List<Long> userIds = List.of(3L, 4L);
    List<User> emptyUserList = Collections.emptyList();
    List<UserDto> emptyDtoList = Collections.emptyList();

    when(userRepository.findByIdIn(userIds)).thenReturn(emptyUserList);
    when(userMapper.toDtoList(emptyUserList)).thenReturn(emptyDtoList);

    List<UserDto> result = userService.findByIds(userIds);

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(userRepository).findByIdIn(userIds);
    verify(userMapper).toDtoList(emptyUserList);
  }

  @Test
  void findAll_ShouldReturnAllUserDtos() {
    User user2 = new User();
    user2.setId(2L);
    UserDto userDto2 =
        new UserDto(
            2L,
            "Jane",
            "Smith",
            LocalDate.of(1991, 2, 2),
            "jane@example.com",
            Collections.emptyList());

    List<User> userList = List.of(user, user2);
    List<UserDto> dtoList = List.of(userDto, userDto2);

    when(userRepository.findAll()).thenReturn(userList);
    when(userMapper.toDtoList(userList)).thenReturn(dtoList);

    List<UserDto> result = userService.findAll();

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(userRepository).findAll();
    verify(userMapper).toDtoList(userList);
  }

  @Test
  void findAll_ShouldReturnEmptyList_WhenNoUsersExist() {
    List<User> emptyUserList = Collections.emptyList();
    List<UserDto> emptyDtoList = Collections.emptyList();

    when(userRepository.findAll()).thenReturn(emptyUserList);
    when(userMapper.toDtoList(emptyUserList)).thenReturn(emptyDtoList);

    List<UserDto> result = userService.findAll();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(userRepository).findAll();
    verify(userMapper).toDtoList(emptyUserList);
  }
}
