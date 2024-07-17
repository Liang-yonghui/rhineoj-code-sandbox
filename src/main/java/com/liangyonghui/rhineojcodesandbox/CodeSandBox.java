package com.liangyonghui.rhineojcodesandbox;

import com.liangyonghui.rhineojcodesandbox.model.ExecuteCodeRequest;
import com.liangyonghui.rhineojcodesandbox.model.ExecuteCodeResponse;

public interface CodeSandBox {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
