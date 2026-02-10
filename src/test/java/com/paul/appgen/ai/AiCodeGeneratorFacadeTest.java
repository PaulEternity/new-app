package com.paul.appgen.ai;

import com.paul.appgen.core.AiCodeGeneratorFacade;
import com.paul.appgen.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;


@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

/**
 * 测试生成代码的方法
 * 验证AI代码生成器能否成功生成并保存登录页面的HTML代码
 */
    @Test
    void generateCode() {
        // 调用AI代码生成器门面的生成并保存代码方法
        // 参数为：需求描述"生成一个登录页面"和代码类型HTML
        // 返回生成的文件对象
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登录页面", CodeGenTypeEnum.HTML,0L);
        // 验证生成的文件对象不为空，即代码生成成功
        Assertions.assertNotNull(file);
    }

    @Test
    void generateMultiFileCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登录页面,不超过20行", CodeGenTypeEnum.MULTI_FILE,0L);
        Assertions.assertNotNull(file);
    }
}

