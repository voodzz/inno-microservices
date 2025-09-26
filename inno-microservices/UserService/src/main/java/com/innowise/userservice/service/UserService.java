package com.innowise.userservice.service;

import com.innowise.userservice.model.dto.UserDto;

public interface UserService extends CrudService<UserDto, Long>{
    UserDto findByEmail(String email);
}
