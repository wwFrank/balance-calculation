package com.hsbc.calculation.config;

import com.google.j2objc.annotations.Property;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Property("spring.redis.host")
    private String redisHost;

    @Property("spring.redis.username")
    private String username;

    @Property("spring.redis.password")
    private String password;

    @Property("spring.redis.timeout")
    private long timeout;

    /**
     *  TCP_KEEPALIVE打开，并且配置三个参数分别为:
     *  TCP_KEEPIDLE = 30
     *  TCP_KEEPINTVL = 10
     *  TCP_KEEPCNT = 3
     */
    private static final int TCP_KEEPALIVE_IDLE = 30;

    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = getRedisStandaloneConfiguration();

        SocketOptions socketOptions = SocketOptions.builder()
                .keepAlive(SocketOptions.KeepAliveOptions.builder()
                        .enable()
                        .idle(Duration.ofSeconds(TCP_KEEPALIVE_IDLE))
                        .interval(Duration.ofSeconds(TCP_KEEPALIVE_IDLE / 3))
                        .count(3)
                        .build()).connectTimeout(Duration.ofSeconds(5000)).build();

        LettuceClientConfiguration lettuceClientConfiguration = LettuceClientConfiguration.builder().clientOptions(
                ClientOptions.builder().socketOptions(socketOptions).build()).build();
        return new LettuceConnectionFactory(config, lettuceClientConfiguration);
    }

    private static RedisStandaloneConfiguration getRedisStandaloneConfiguration() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("r-uf67ubzdbg68iy0duspd.redis.rds.aliyuncs.com");
        config.setPort(6379);
        config.setUsername("r-uf67ubzdbg68iy0dus");
        config.setPassword("Frank!@#");
        return config;
    }

    @Bean
    RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
