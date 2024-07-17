package com.liangyonghui.rhineojcodesandbox.docker;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.liangyonghui.rhineojcodesandbox.CodeSandBox;
import com.liangyonghui.rhineojcodesandbox.model.ExecuteCodeRequest;
import com.liangyonghui.rhineojcodesandbox.model.ExecuteCodeResponse;
import com.liangyonghui.rhineojcodesandbox.model.ExecuteMessgae;
import com.liangyonghui.rhineojcodesandbox.model.JudgeInfo;
import com.liangyonghui.rhineojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

//@SpringBootApplication
public class JavaDockerCodeSandbox implements CodeSandBox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final long TIME_OUT = 5000L;

    public static void main(String[] args) {
        JavaDockerCodeSandbox javaNativeCodeSandbox = new JavaDockerCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);

    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        // 编译代码，得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());

        try {
            Process complieProcess = Runtime.getRuntime().exec(compileCmd);
            //超时控制
            new Thread(()->{
                try {
                    Thread.sleep(TIME_OUT);
                    System.out.println("超时了，中断");
                    complieProcess.destroy();
                }catch  (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            ExecuteMessgae executeMessgae = ProcessUtils.runProcessAndGetMessage(complieProcess, "编译");
            System.out.println(executeMessgae);
        } catch (Exception e) {
            return getErrorResponse(e);
        }

        // 执行代码，得到输出结果
        ArrayList<ExecuteMessgae> executeMessgaesList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -D -cp %s Main %s", userCodeParentPath, inputArgs);
            try{
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                ExecuteMessgae executeMessgae = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessgae);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        
        // 收集整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        for (ExecuteMessgae executeMessgae : executeMessgaesList) {
            String errorMessage = executeMessgae.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                // 执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessgae.getMessage());
            Long time = executeMessgae.getTime();
            if(time != null){
                maxTime = Math.max(maxTime, time);
            }
        }
        // 正常运行完成
        if (outputList.size() == executeMessgaesList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        //judgeInfo.setMemory();
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 文件清理
        if (userCodeFile.getParentFile() != null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }


        return executeCodeResponse;
    }

    /**
     * 获取错误方法
     * @param
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;

    }
}
