package com.paul.appgen.ai;

import com.paul.appgen.ai.model.HtmlCodeResult;
import com.paul.appgen.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHTMLCode(){
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("做一个个人博客，不超过20行");
        Assertions.assertNotNull(result);
    }
    @Test
    void generateMultiFileCode(){
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做一个留言板，不超过50行");
        Assertions.assertNotNull(result);
    }
}
