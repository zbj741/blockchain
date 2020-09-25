package com.buaa.blockchain.test;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.entity.ContractAccount;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class LoadClassTest {

    public static void main(String[] args) {


        WorldState worldState = new WorldState("D:\\data","triedb",null);
        LoadTest2(worldState);
    }

    public static void LoadTest2(State state){
        String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
        String contractName = "com.buaa.blockchain.contract.develop.Add";
        String softPath = "file:"+contractDir+"Add.class";
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL(softPath)},Thread.currentThread().getContextClassLoader());
            Class demo = classLoader.loadClass(contractName);
            Contract object = (Contract) demo.newInstance();
            Map<String, DataUnit> arg = new HashMap<>();
            arg.put("KEY",new DataUnit("key"));
            arg.put("VALUE_1",new DataUnit(654));
            arg.put("VALUE_2",new DataUnit(234));
            object.initParam(arg);
            object.run(state);
            System.out.println(state.get("key"));
            System.out.println("EXECUTE");
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    public static void LoadTest(State state){

        String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
        String contractName = "com.buaa.blockchain.contract.develop.Add";
        File fileln = new File (contractDir+"Add.class"); //打开源文件
        byte[] origindata = null;
        try{
            FileInputStream streamln = new FileInputStream (fileln); //根据源文件构建输入流
            origindata = new byte[streamln.available()];
            streamln.read(origindata);
            State worldState = state;
//            WorldState worldState = new WorldState("D:\\data","triedb");
//            //worldState.update("ID","0");
//            //写入 key数据是自我编写，后期可能需要调整
            worldState.update("C20200901000001",origindata);
            byte[] data = origindata;
            Class clazz = ByteClassLoader.getClass(data,contractName);
            // Class clazz = FileClassLoader.getClass(contractDir+"Add.class",contractName);
            System.out.println(clazz);
            Object _obj = clazz.newInstance();
            Contract cobj = (Contract) _obj;
            Map<String, DataUnit> arg = new HashMap<>();
            arg.put("KEY",new DataUnit("key"));
            arg.put("VALUE_1",new DataUnit(654));
            arg.put("VALUE_2",new DataUnit(234));
            cobj.initParam(arg);
            cobj.run(worldState);
            System.out.println(worldState.get("key"));


            ContractAccount contractAccount = new ContractAccount("Add","Add",data);
            contractAccount.load();
            Contract cobj1 = (Contract) contractAccount.getClazz().newInstance();
            cobj1.initParam(arg);
            cobj1.run(worldState);
            System.out.println(worldState.get("key"));

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
