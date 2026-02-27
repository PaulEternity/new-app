package com.paul.appgen.core;

import com.paul.appgen.ai.AiCodeGeneratorFactory;
import com.paul.appgen.ai.AiCodeGeneratorService;
import com.paul.appgen.ai.model.HtmlCodeResult;
import com.paul.appgen.ai.model.MultiFileCodeResult;
import com.paul.appgen.core.parser.CodeParserExecutor;
import com.paul.appgen.core.saver.CodeFileSaverExecutor;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorFactory aiCodeGeneratorFactory;

    /**
     * 统一入口
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield  CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口，根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId,userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.VUE_PROJECT,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }


    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType,Long appId) {

        // 字符串拼接器，用于流式返回完成之后再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        // 处理代码流
        return codeStream.doOnNext(chunk -> {
            // 将每个代码块追加到字符串构建器中
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            try {
                //流式返回完成后保存代码
                // 将拼接的完整HTML代码转换为字符串
                String completeCode = codeBuilder.toString();
                //解析代码为对象
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                //保存代码到文件
                File saveDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                // 记录保存成功的日志
                log.info("保存成功！保存目录:{}", saveDir.getAbsolutePath());
            } catch (Exception e) {
                // 记录保存失败的日志
                log.error("保存失败！", e.getMessage());
            }
        });
    }


    /**
     * 生成并保存HTML代码文件
     *
     * @param userMessage 用户输入的消息内容，用于生成HTML代码
     * @return 返回生成的HTML文件对象
     */
//    @Deprecated
//    private File generateAndSaveHtmlCode(String userMessage) {
//        // 调用AI代码生成服务生成HTML代码
//        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
//        // 保存HTML文件（第一次保存，但返回值未被使用）
//        CodeFileSaver.saveHtmlFile(htmlCodeResult);
//        // 再次保存HTML文件并返回文件对象
//        return CodeFileSaver.saveHtmlFile(htmlCodeResult);
//    }


    /**
     * 生成并保存多文件代码的主方法
     * 该方法接收用户输入的消息，调用AI代码生成服务生成多文件代码，
     * 然后通过代码文件保存服务将生成的代码保存到文件系统中
     *
     * @param userMessage 用户输入的消息，用于生成代码的依据
     * @return File 返回保存生成的代码文件后的文件对象
     */
//    @Deprecated
//    private File generateAndSaveMultiFileCode(String userMessage) {
//        // 调用AI代码生成服务生成多文件代码
//        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//        // 调用代码文件保存服务，将生成的代码保存到文件系统并返回文件对象
//        return CodeFileSaver.saveMultiFileCode(result);
//    }

    /**
     * 生成并保存多文件代码流的方法
     * 该方法通过AI生成代码流，并在流完成后保存生成的代码
     *
     * @param userMessage 用户输入的消息，用于生成代码
     * @return 返回一个Flux<String>，表示生成的代码流
     */
//    @Deprecated
//    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage,Long appId) {
//        Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//        return processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE,appId);
//    }

    /**
     * 生成并保存HTML代码流
     * 该方法通过AI服务生成HTML代码流，并在流式返回完成后将完整代码保存到文件
     *
     * @param userMessage 用户输入的消息，用于生成HTML代码
     * @return 返回一个Flux<String>，表示生成的HTML代码流
     */
//    @Deprecated
//    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage,Long appId) {
//        Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
//        return processCodeStream(codeStream, CodeGenTypeEnum.HTML,appId);
//    }
}
