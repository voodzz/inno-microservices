package com.innowise.authservice.service;

import com.innowise.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for managing users and integrating with Spring Security.
 *
 * <p>Implements {@link UserDetailsService} to load user details for authentication based on the
 * username.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;

  /**
   * Loads a user by username for authentication purposes.
   *
   * @param username the username of the user to load
   * @return the {@link UserDetails} of the user
   * @throws UsernameNotFoundException if no user with the given username is found
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user: " + username));
  }
}
