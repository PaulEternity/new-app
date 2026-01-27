package com.paul.appgen.ai;

import com.paul.appgen.ai.model.HtmlCodeResult;
import com.paul.appgen.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 生成HTML代码
     * @param userMessage 用户输入
     * @return AI的输出结果
     * @param userMessage
     * @return
     */
    @SystemMessage(value = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户输入
     * @return AI的输出结果
     * @param userMessage
     * @return
     */
    @SystemMessage(value = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成HTML代码
     * @param userMessage 用户输入
     * @return AI的输出结果
     * @param userMessage
     * @return
     */
    @SystemMessage(value = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户输入
     * @return AI的输出结果
     * @param userMessage
     * @return
     */
    @SystemMessage(value = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
