package com.innowise.authservice.service;

import com.innowise.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RollbackService {
    private final UserRepository userRepository;

    @Transactional
    public void rollbackAuthUserCreation(String username) {
        userRepository.deleteByUsername(username);
    }
}
