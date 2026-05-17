package io.fintech.loan.application.service;

import jakarta.persistence.EntityManagerFactory;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Test-only beans so unit tests can boot the Camel context without real backends.
 *
 * <p>The actual backend endpoints (jpa:, spring-redis:, kafka:, http:) are replaced
 * by AdviceWith in each test — these mocks just satisfy bean-wiring and endpoint
 * creation requirements at context-start time.
 */
@TestConfiguration
@Profile("test")
public class UnitTestConfig {

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return Mockito.mock(RedisConnectionFactory.class);
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

  @Bean
  public EntityManagerFactory entityManagerFactory() {
    return Mockito.mock(EntityManagerFactory.class);
  }

  @Bean
  public PlatformTransactionManager transactionManager() {
    return Mockito.mock(PlatformTransactionManager.class);
  }
}
