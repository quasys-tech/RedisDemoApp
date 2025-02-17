package com.quasys.redis.Config;

import com.quasys.redis.service.RedisSubscriber;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

//    @Value("${spring.redis.host}")
//    private String redisHost;
//
//    @Value("${spring.redis.port}")
//    private int redisPort;

    private String redisHost = System.getenv("REDIS_HOST");
    private int redisPort = Integer.parseInt(System.getenv("REDIS_PORT"));

    private String redisUser = System.getenv("REDIS_USERNAME");
    private String redisPass = System.getenv("REDIS_PASS");
    private static String redisPrefix = System.getenv("REDIS_PREFIX");
    private static final String PREFIX = System.getenv("REDIS_PREFIX").trim();
    private  String usePrefix = System.getenv("REDIS_USE_PREFIX");
    private String redisConf = System.getenv("REDIS_CONNECTION_CONFIG");
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(redisHost);
        redisConfiguration.setPort(redisPort);
        if (redisConf.trim().toUpperCase().equals("PASSWORDONLY")){
            redisConfiguration.setPassword(redisPass);
        } else if (redisConf.trim().toUpperCase().equals("ACL")) {
            redisConfiguration.setUsername(redisUser);
            redisConfiguration.setPassword(redisPass);
        }
        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        if (usePrefix.trim().toLowerCase().equals("true")) {
            System.out.println(PREFIX);
            keySerializer = new StringRedisSerializer() {
                @Override
                public byte[] serialize(String key) {
                    String prefixedKey = PREFIX + key;
                    return super.serialize(prefixedKey);
                }

                @Override
                public String deserialize(byte[] bytes) {
                    String deserializedKey = super.deserialize(bytes);
                    if (deserializedKey != null && deserializedKey.startsWith(PREFIX)) {
                        return deserializedKey.substring(PREFIX.length());
                    }
                    return deserializedKey;
                }
            };
        }
        StringRedisSerializer valueSerializer = new StringRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        return template;
    }
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
//        return template;
//    }


//    @Bean
//    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
//                                                        MessageListenerAdapter listenerAdapter, ChannelTopic topic) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(redisConnectionFactory());
//        container.addMessageListener(listenerAdapter, topic);
//        return container;
//    }
//
//    @Bean
//    public MessageListenerAdapter listenerAdapter(RedisSubscriber redisSubscriber) {
//        return new MessageListenerAdapter(redisSubscriber);
//    }
//
//    @Bean
//    public ChannelTopic topic() {
//        return new ChannelTopic("updates");
//    }
}
