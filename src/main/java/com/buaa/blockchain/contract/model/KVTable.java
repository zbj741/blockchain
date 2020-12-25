package com.buaa.blockchain.contract.model;

import java.util.Map;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/22
 * @since JDK1.8
 */
public class KVTable {
    private Map<String, Entry> data;

    public KVTable(Map<String, Entry> data) {
       this.data = data;
    }

    public Entry get(String id){
        return data.get(id);
    }

    public void set(String id, Entry entry){
        Entry item = this.get(id);
        if(item==null){
            item = new Entry();
        }
    }

    public Entry newEntry(){
        return new Entry();
    }
}
