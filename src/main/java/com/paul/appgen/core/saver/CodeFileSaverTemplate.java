package com.paul.appgen.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.paul.appgen.exception.BusinessException;
import com.paul.appgen.exception.ErrorCode;
import com.paul.appgen.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码文件保存器  模板方法模式
 */
public abstract class CodeFileSaverTemplate<T> {
    private static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    public final File saveCode(T result){
        validateInput(result);

        String baseDirPath = buildUniquePath();

        saveFiles(result, baseDirPath);

        return new File(baseDirPath);
    }



    protected void validateInput(T result) {
        if(result == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析结果为空");
        }
    }

    //构建文件的唯一路径
    private String buildUniquePath() {
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    //保存单个文件
    public final void writeToFile(String dirPath,String fileName, String fileContent) {
        if (StrUtil.isNotBlank(fileContent)) {
            String filePath = dirPath + File.separator + fileName;
            FileUtil.writeString(fileContent,filePath, StandardCharsets.UTF_8);
        }
    }

    protected abstract CodeGenTypeEnum getCodeType();

    protected abstract void saveFiles(T result, String baseDirPath);
}
