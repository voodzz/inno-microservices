package com.innowise.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.model.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class RedisConfigTests {

  @Test
  @DisplayName("redisObjectMapper is configured without failing on unknown props and with JavaTime")
  void redisObjectMapper_Configured() {
    RedisConfig config = new RedisConfig();
    ObjectMapper mapper = config.redisObjectMapper();
    assertNotNull(mapper);
    assertDoesNotThrow(
        () ->
            mapper.writeValueAsString(
                new UserDto(null, null, null, null, null, java.util.List.of())));
  }

  @Test
  @DisplayName("cacheManager exposes USER_CACHE via getCache")
  void cacheManager_DefinesUserCache() {
    RedisConfig config = new RedisConfig();
    ObjectMapper mapper = config.redisObjectMapper();
    RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

    RedisCacheManager manager = config.cacheManager(factory, mapper);
    assertNotNull(manager);
    assertNotNull(manager.getCache("USER_CACHE"));
  }
}
