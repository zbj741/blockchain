package com.buaa.blockchain.utils;

import lombok.Data;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/15
 * @since JDK1.8
 */
@Data
public class ResultMsg<T> {
    private boolean success;
    private int code;
    private  String msg;
    private T data;

    //自定义异常返回的结果
    public static ResultMsg defineError(MyExceptionCode de){
        ResultMsg result = new ResultMsg();
        result.setSuccess(false);
        result.setCode(de.getErrorCode());
        result.setMsg(de.getErrorMsg());
        result.setData(null);
        return result;
    }
    //其他异常处理方法返回的结果
    public static ResultMsg otherError(ResultCode errorEnum){
        ResultMsg result = new ResultMsg();
        result.setMsg(errorEnum.getErrorMsg());
        result.setCode(errorEnum.getErrorCode());
        result.setSuccess(false);
        result.setData(null);
        return result;
    }

}

