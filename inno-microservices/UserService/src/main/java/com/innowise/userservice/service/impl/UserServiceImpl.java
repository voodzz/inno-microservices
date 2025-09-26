package com.innowise.userservice.service.impl;

import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.exception.UpdateException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.UserDto;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserDto create(UserDto request) {
    User user = userMapper.toEntity(request);
    return userMapper.toDto(userRepository.save(user));
  }

  @Override
  public UserDto findById(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> findByIds(Collection<Long> ids) {
    return userMapper.toDtoList(userRepository.findByIdIn(ids));
  }

  @Override
  public UserDto findByEmail(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(NotFoundException::new);
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
  }

  @Override
  @Transactional
  public void delete(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    userRepository.delete(user);
  }
}
