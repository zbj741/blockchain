package com.buaa.blockchain.contract.model;

import java.util.HashMap;
import java.util.Map;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/22
 * @since JDK1.8
 */
public class Entry {
    Map<String, Object> value = new HashMap<>();

    public int getInt(String itemCode){
       return (int) value.get(itemCode);
    }

    public String getString(String itemCode){
        return (String) value.get(itemCode);
    }

    public byte[] getBytes(String itemCode){
        return (byte[]) value.get(itemCode);
    }


    public void setInt(String itemCode, int itemVal){
        value.put(itemCode, itemVal);
    }

    public void setString(String itemCode, String itemVal){
        value.put(itemCode, itemVal);
    }

    public void setBytes(String itemCode, byte[] itemVal){
        value.put(itemCode, itemVal);
    }
}
