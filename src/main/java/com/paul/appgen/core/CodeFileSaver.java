package com.paul.appgen.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.paul.appgen.ai.model.HtmlCodeResult;
import com.paul.appgen.ai.model.MultiFileCodeResult;
import com.paul.appgen.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class CodeFileSaver {

    //文件保存的根目录
    private static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    //保存HTML网页代码
    public static File saveHtmlFile(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniquePath(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }

    //保存多文件代码
    public static File saveMultiFileCode(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniquePath(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }

    //构建文件的唯一路径
    private static String buildUniquePath(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    //保存单个文件
    private static void writeToFile(String dirPath,String fileName, String fileContent) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(fileContent,filePath, StandardCharsets.UTF_8);
    }
}
