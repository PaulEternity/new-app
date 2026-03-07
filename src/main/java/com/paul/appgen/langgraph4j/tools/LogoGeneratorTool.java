package com.paul.appgen.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.paul.appgen.langgraph4j.model.ImageResource;
import com.paul.appgen.langgraph4j.model.enums.ImageCategoryEnum;
import com.paul.appgen.manager.CosManager;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Logo 图片生成工具
 */
@Slf4j
@Component
public class LogoGeneratorTool {

    @Resource
    private CosManager cosManager;

    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model:wan2.2-t2i-flash}")
    private String imageModel;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            // 构建 Logo 设计提示词
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .prompt(logoPrompt)
                    .size("512*512")
                    .n(1)
                    .build();
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);
            if (result != null && result.getOutput() != null && result.getOutput().getResults() != null) {
                List<Map<String, String>> results = result.getOutput().getResults();
                for (Map<String, String> imageResult : results) {
                    String imageUrl = imageResult.get("url");
                    if (StrUtil.isNotBlank(imageUrl)) {
                        // 下载图片到临时文件
                        File imageFile = downloadImage(imageUrl);
                        if (imageFile != null && imageFile.exists()) {
                            // 上传到 COS
                            String keyName = String.format("/logo/%s/%s_%s.%s",
                                    RandomUtil.randomString(5),
                                    System.currentTimeMillis(),
                                    RandomUtil.randomString(4),
                                    FileUtil.extName(imageFile));
                            String cosUrl = cosManager.uploadFile(keyName, imageFile);
                            // 清理临时文件
                            FileUtil.del(imageFile);
                            if (StrUtil.isNotBlank(cosUrl)) {
                                logoList.add(ImageResource.builder()
                                        .category(ImageCategoryEnum.LOGO)
                                        .description(description)
                                        .url(cosUrl)
                                        .build());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }

    /**
     * 从 URL 下载图片到临时文件
     */
    private File downloadImage(String imageUrl) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                String ext = "png";
                if (imageUrl.contains(".jpg") || imageUrl.contains(".jpeg")) {
                    ext = "jpg";
                } else if (imageUrl.contains(".webp")) {
                    ext = "webp";
                }
                File tempFile = FileUtil.createTempFile("logo_", "." + ext, true);
                Files.write(tempFile.toPath(), response.body());
                return tempFile;
            } else {
                log.error("下载图片失败，HTTP状态码: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.error("下载图片失败: {}", e.getMessage(), e);
        }
        return null;
    }
}