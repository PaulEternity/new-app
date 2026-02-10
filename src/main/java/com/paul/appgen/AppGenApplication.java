package com.paul.appgen;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.paul.appgen.mapper")
public class AppGenApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppGenApplication.class, args);
    }

}
