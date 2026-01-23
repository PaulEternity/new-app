package com.paul.appgen.core;

import com.paul.appgen.ai.AiCodeGeneratorService;
import com.paul.appgen.ai.model.HtmlCodeResult;
import com.paul.appgen.ai.model.MultiFileCodeResult;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 代码生成门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, errorMessage);
            }
        };
    }

/**
 * Generates HTML code based on user message and saves it to a file.
 * This method utilizes the AI code generator service to create HTML code
 * and then saves the generated code to a file using the CodeFileSaver utility.
 *
 * @param userMessage The input message from the user that will be used to generate HTML code
 * @return File object representing the saved HTML file
 */
    private File generateAndSaveHtmlCode(String userMessage) {
    // Generate HTML code using AI service based on user message
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
    // Save the generated HTML code to a file
        CodeFileSaver.saveHtmlFile(htmlCodeResult);
    // Return the saved HTML file (Note: this line saves the file again, which might be redundant)
        return CodeFileSaver.saveHtmlFile(htmlCodeResult);
    }
/**
 * This method generates and saves code for multiple files based on the user's message.
 * It takes a user message as input and processes it to create the necessary code files.
 *
 * @param userMessage The input message from the user that contains the requirements for generating code
 * @return A String representing the generated code or status of the operation
 */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return  CodeFileSaver.saveMultiFileCode(result);
    }
}
