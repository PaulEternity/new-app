package com.paul.appgen.core;

import com.paul.appgen.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

/**
 * 测试方法：验证代码生成功能
 * 通过AI代码生成器生成指定要求的代码，并验证生成结果
 */
    @Test
    void generate() {
    // 使用AI代码生成器生成一个20行以内的登录HTML页面
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个登录页面，20行代码以内", CodeGenTypeEnum.HTML, 1L);
    // 收集代码流中的所有生成的代码片段并阻塞等待结果
        List<String> result = codeStream.collectList().block();
        Assertions.assertNotNull(result);
        String join = String.join("", result);
        Assertions.assertNotNull(join);
    }
}