package com.paul.appgen.ai;

import com.paul.appgen.ai.model.HtmlCodeResult;
import com.paul.appgen.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {


/**
 * 生成HTML代码的方法
 * 该方法使用系统提示文件来指导代码生成
 * @param userMessage 用户输入的消息，作为生成HTML代码的依据
 * @return HtmlCodeResult 包含生成HTML代码的结果对象
 */
    @SystemMessage(value = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(@UserMessage String userMessage);

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

    @SystemMessage(value = "prompt/codegen-vue-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId Long appId, @UserMessage String userMessage);
}
