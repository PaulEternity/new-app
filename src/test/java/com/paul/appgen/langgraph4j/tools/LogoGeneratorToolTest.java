package com.paul.appgen.langgraph4j.tools;

import com.paul.appgen.langgraph4j.model.ImageResource;
import com.paul.appgen.langgraph4j.model.enums.ImageCategoryEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LogoGeneratorToolTest {

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

/**
 * 测试生成logo的方法
 * 验证logo生成功能是否正常工作，包括检查生成的logo列表是否为空，
 * 以及每个logo对象的属性是否符合预期
 */
    @Test
    void generateLogos() {
        // 调用logo生成工具生成描述为"来个心碎logo"的logo列表
        List<ImageResource> logos = logoGeneratorTool.generateLogos("来个心碎logo");
        // 验证生成的logo列表不为空
        assertNotNull(logos);
        // 获取第一个logo对象
        ImageResource firstLogo = logos.getFirst();
        // 验证第一个logo的类别为LOGO
        assertEquals(ImageCategoryEnum.LOGO, firstLogo.getCategory());
        // 验证第一个logo的描述不为空
        assertNotNull(firstLogo.getDescription());
        // 验证第一个logo的URL不为空
        assertNotNull(firstLogo.getUrl());
        // 遍历并打印所有logo的描述和URL
        logos.forEach(logo ->
                System.out.println("Logo:" + logo.getDescription()+"-" + logo.getUrl())
        );
    }
}