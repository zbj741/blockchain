package com.buaa.blockchain.contract.model;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/23
 * @since JDK1.8
 */
public class CallMethod {
    private String method;
    private Object[] params;

    public CallMethod() {
    }

    public CallMethod(String method, Object[] params) {
        this.method = method;
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public Object[] getParams() {
        return params;
    }
}
