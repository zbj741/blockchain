package com.buaa.blockchain.utils;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.message.Message;
import com.buaa.blockchain.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.buaa.blockchain.entity.Transaction.createDefaultTransaction;

@Service
public class JsonUtil {
    public static ObjectMapper objectMapper = new ObjectMapper();
    static{
        // 转化为格式化的json
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 如果json中有新增的字段并且在实体类中不存在，不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    /**
     * 将需要广播的内容打包成为字符串形式
     * */
    public static synchronized String message2JsonString(Message message){
        String jsonStr = "";
        try {
            jsonStr = JsonUtil.objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonStr;
    }

    public static void main(String[] args) {
        Map<String,String> map = new HashMap<>();
        map.put("233","666");
        map.put("114514","ah ah ah ahhhhhhhhhhh");
        String jsonStr = "";

        try {
            jsonStr = JsonUtil.objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(jsonStr);


        try {
            Map<String,String> map2 = JsonUtil.objectMapper.readValue(jsonStr, new TypeReference<Map<String, String>>() {});
            System.out.println(map2.get("233"));


            Transaction transaction1 = createDefaultTransaction();
            Transaction transaction2 = createDefaultTransaction();
            Transaction transaction3 = createDefaultTransaction();
            ArrayList<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction1);
            transactions.add(transaction2);
            transactions.add(transaction3);
            Block block = new Block();
            block.setArgs("preHash","hash","merkleRoot","preMerkleRoot",1,"sign",1l,"version",transactions, transactions.size());
            String bstr = objectMapper.writeValueAsString(block);
            Block block1 = (Block) objectMapper.readValue(bstr,Block.class);
            System.out.println(block1);

            Message message = new Message("233","testNode",1l,0l,true,block);
            String messagestr = objectMapper.writeValueAsString(message);
            System.out.println(messagestr);
            Message message1 = objectMapper.readValue(messagestr,Message.class);
            System.out.println(message1.toString());


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
