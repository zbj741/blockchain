package com.buaa.blockchain.contract.component;

import java.util.List;
import java.util.StringJoiner;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/6
 * @since JDK1.8
 */
public abstract class IContract {

    private  List<String> LOGS;

    public void LOG(String topic, String msg){
        LOGS.add(topic+":"+msg);
    }

    public void LOG(String topic, Object... value){
        LOG(topic, createEventLog(value));
    }

    private String createEventLog(Object... value) {
        StringJoiner logJoiner = new StringJoiner(",");
        for (Object item : value) {
            logJoiner.add(String.valueOf(item));
        }
        return logJoiner.toString();
    }
}
