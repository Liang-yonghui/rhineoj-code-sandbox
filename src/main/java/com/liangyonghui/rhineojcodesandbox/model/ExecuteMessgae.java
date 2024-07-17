package com.liangyonghui.rhineojcodesandbox.model;

import lombok.Data;

/**
 * 判题信息
 */
@Data
public class ExecuteMessgae {


    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;
}
