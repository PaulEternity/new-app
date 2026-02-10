package com.paul.appgen.config;


import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis聊天记忆存储配置类
 * 用于配置Redis连接参数并创建RedisChatMemoryStore Bean
 */
@Configuration // 标识这是一个配置类
@ConfigurationProperties(prefix = "spring.data.redis") // 绑定配置文件中以"spring.data.reds"为前缀的属性
@Data // 使用Lombok自动生成getter、setter等方法
public class RedisChatMemoryStoreConfig {

    private String host; // Redis服务器主机地址

    private int port; // Redis服务器端口号

    private String password; // Redis服务器密码

    private int database; // Redis数据库索引

    private long timeout; // 连接超时时间(毫秒)


    /**
     * 创建并配置RedisChatMemoryStore Bean
     * @return 配置好的RedisChatMemoryStore实例
     */
    @Bean // 标识该方法返回一个Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host) // 设置主机地址
                .password(password) // 设置密码
                .port(port) // 设置端口号
                .ttl(timeout) // 设置生存时间
                .build(); // 构建并返回RedisChatMemoryStore实例
    }
}
