package com.paul.appgen.core.parser;

import com.paul.appgen.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HtmlCodeParser 类，用于解析包含HTML代码的内容
 * 实现了 CodeParser 接口，专门处理 HTML 代码块的解析
 *
 * @param
 */
public class HtmlCodeParser implements CodeParser<HtmlCodeResult>{
    // 使用正则表达式匹配HTML代码块，支持大小写不敏感的匹配
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析包含HTML代码的内容
     *
     * @param codeContent 需要解析的原始内容字符串
     * @return 包含解析结果的 HtmlCodeResult 对象
     */
    @Override
    public  HtmlCodeResult parseCode(String codeContent) {
        // 创建结果对象
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 提取 HTML 代码内容
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
