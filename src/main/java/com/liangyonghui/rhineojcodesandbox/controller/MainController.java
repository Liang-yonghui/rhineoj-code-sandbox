package com.liangyonghui.rhineojcodesandbox.controller;


import com.liangyonghui.rhineojcodesandbox.JavaNativeCodeSandbox;
import com.liangyonghui.rhineojcodesandbox.model.ExecuteCodeRequest;
import com.liangyonghui.rhineojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {

    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";


    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;


    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody  ExecuteCodeRequest executeCodeRequest, HttpServletRequest request
            , HttpServletResponse response){
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)){
            response.setStatus(403);
            return null;
        }

        if(executeCodeRequest == null){
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
