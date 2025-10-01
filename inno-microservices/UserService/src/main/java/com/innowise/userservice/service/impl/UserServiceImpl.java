package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.AlreadyExistsException;
import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.exception.UpdateException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
  private static final String CACHE_NAME = "USER_CACHE";

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final CacheManager cacheManager;
  private Cache cache;

  @PostConstruct
  public void init() {
    cache =
        Optional.ofNullable(cacheManager.getCache(CACHE_NAME))
            .orElseThrow(
                () -> new IllegalStateException("Cache '" + CACHE_NAME + "' not configured"));
  }

  @Override
  @Transactional
  public UserDto create(UserDto request) {
    User user = userMapper.toEntity(request);

    if (userRepository.existsByEmail(request.email())) {
      throw new AlreadyExistsException(
          "User with email '%s' already exists".formatted(request.email()));
    }

    UserDto dto = userMapper.toDto(userRepository.save(user));

    cache.put(dto.id(), dto);
    cache.put(dto.email(), dto);
    return dto;
  }

  @Override
  @Cacheable(value = CACHE_NAME, key = "#id")
  public UserDto findById(Long id) {
    User user = userRepository.findByIdWithCards(id).orElseThrow(() -> new NotFoundException(id));
    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> findByIds(Collection<Long> ids) {
    return userMapper.toDtoList(userRepository.findByIdIn(ids));
  }

  @Override
  @Cacheable(value = CACHE_NAME, key = "#email")
  public UserDto findByEmail(String email) {
    User user =
        userRepository
            .findByEmailWithCards(email)
            .orElseThrow(
                () -> new NotFoundException("User with email '%s' not found".formatted(email)));
    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> findAll() {
    return userMapper.toDtoList(userRepository.findAll());
  }

  @Override
  @Transactional
  public void update(Long id, UserDto request) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id));

    int updated =
        userRepository.updateById(
            user.getId(), request.name(), request.surname(), request.birthDate());

    if (updated == 0) {
      throw new UpdateException(user.getId());
    }

    User updatedUser = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    UserDto updatedDto = userMapper.toDto(updatedUser);

    cache.put(updatedDto.id(), updatedDto);
    cache.put(updatedDto.email(), updatedDto);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id));

    cache.evict(user.getId());
    cache.evict(user.getEmail());

    userRepository.delete(user);
  }
}
