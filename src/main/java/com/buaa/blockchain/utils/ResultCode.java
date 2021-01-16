package com.buaa.blockchain.utils;

public enum ResultCode {
    SUCCESS(200, "成功"),
    NO_PERMISSION(403,"暂无权限"),
    NO_AUTH(401,"请先登录"),
    NOT_FOUND(404, "未找到该资源!"),
    INTERNAL_SERVER_ERROR(500, "服务器出错"),
    ;
    /** 错误码 */
    private Integer errorCode;

    /** 错误信息 */
    private String errorMsg;

    ResultCode(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}

