package com.innowise.userservice.service;

import com.innowise.userservice.exception.NotFoundException;
import com.innowise.userservice.exception.UpdateException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.model.dto.CreateUserRequest;
import com.innowise.userservice.model.dto.UpdateUserRequest;
import com.innowise.userservice.model.dto.UserResponse;
import com.innowise.userservice.model.entity.User;
import com.innowise.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional
  public UserResponse createUser(CreateUserRequest request) {
    User user = userMapper.toEntity(request);
    return userMapper.toDto(userRepository.save(user));
  }

  public UserResponse findById(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    return userMapper.toDto(user);
  }

  public List<UserResponse> findByIds(Collection<Long> ids) {
    return userMapper.toDtoList(userRepository.findByIdIn(ids));
  }

  public UserResponse findByEmail(String email) {
    User user =
        userRepository.findByEmail(email).orElseThrow(NotFoundException::new);
    return userMapper.toDto(user);
  }

  public List<UserResponse> findAll() {
    List<User> all = userRepository.findAll();
    return userMapper.toDtoList(all);
  }

  @Transactional
  public void updateUser(UpdateUserRequest request) {
    User user =
        userRepository
            .findById(request.id())
            .orElseThrow(() -> new NotFoundException(request.id()));

    int updated =
        userRepository.updateById(
            user.getId(), request.name(), request.surname(), request.birthDate());

    if (updated == 0) {
      throw new UpdateException(user.getId());
    }
  }

  @Transactional
  public void deleteUser(Long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(id));
    userRepository.delete(user);
  }
}
