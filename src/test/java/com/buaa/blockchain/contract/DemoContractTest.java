package com.buaa.blockchain.contract;

import com.buaa.blockchain.contract.component.ContractException;
import com.buaa.blockchain.contract.component.IContract;
import com.buaa.blockchain.utils.ReflectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
* IContract Tester. 
* 
* @author <Authors name> 
* @since <pre>01/06/2021</pre> 
* @version 1.0 
*/ 
public class DemoContractTest {

    private Map storage = new HashMap();
    private List logList = new ArrayList();
    private Class classZ;
    private Object contract;

    @Before
    public void before() throws Exception {
        this.classZ = Class.forName("DemoContract");
        this.contract = ReflectUtil.getInstance().newInstance(this.classZ, Map.class,  storage);

        if(this.classZ.isAssignableFrom(IContract.class)){
            Field f1 = classZ.getSuperclass().getDeclaredField("LOGS");
            f1.setAccessible(true);
            f1.set(this.contract, this.logList);
        }
    }

    @Test
    public void testAdd(){
        String expectUserId = "11";
        String expectUserName = "zhang";
        invoke("insertUser", new Object[]{
                expectUserId, expectUserName
        });

        Assert.assertTrue(storage.containsKey(expectUserId));
        Assert.assertEquals(expectUserName, storage.get(expectUserId));
    }

    private void invoke(String  method, Object... params){
        try {
            ReflectUtil.getInstance().invoke(classZ, this.contract, method, params);
        }  catch (Exception e){
            if(e.getCause().getClass().isAssignableFrom(ContractException.class)){
                throw new ContractException(e.getCause().getMessage());
            }
            e.printStackTrace();
        }
    }

    private String getTopic(int logIndex){
        String str = (String) this.logList.get(logIndex);
        return str.split(":")[0];
    }

    private <T> T getParam(int logIndex, int paramIndex, Class classType){
        String str = (String) this.logList.get(logIndex);
        String params = str.split(":")[1];
        Object rtnObj = params.split(",")[paramIndex];
        if(classType.isAssignableFrom(String.class)){
            return (T)rtnObj;
        }else if(classType.isAssignableFrom(BigDecimal.class)){
            return (T) new BigDecimal(String.valueOf(rtnObj));
        }else if(classType.isAssignableFrom(Integer.class)) {
            return (T) new Integer(String.valueOf(rtnObj));
        }else if(classType.isAssignableFrom(Long.class)){
            return (T) new Long(String.valueOf(rtnObj));
        }else if(classType.isAssignableFrom(Boolean.class)){
            return (T) Boolean.valueOf(String.valueOf(rtnObj));
        }
        return (T)rtnObj;
    }

    private void printAll(){
        printLogs();
        printStorage();
    }

    private void printLogs(){
        print(this.logList);
    }

    private void printStorage(){
        print(this.storage);
    }

    private void print(Object data){
        try {
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(data));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

} 
