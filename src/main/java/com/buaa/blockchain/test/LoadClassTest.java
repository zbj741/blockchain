package com.buaa.blockchain.test;

import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.account.ContractAccount;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;
import com.buaa.blockchain.contract.util.classloader.FileClassLoader;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class LoadClassTest {
    public static void main(String[] args) {
        String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
        String contractName = "com.buaa.blockchain.contract.develop.Add";
        File fileln = new File (contractDir+"Add.class"); //打开源文件
        byte[] origindata = null;
        try{
            FileInputStream streamln = new FileInputStream (fileln); //根据源文件构建输入流
            origindata = new byte[streamln.available()];
            streamln.read(origindata);
            WorldState worldState = new WorldState("D:\\data","triedb");
            //worldState.update("ID","0");
            //写入 key数据是自我编写，后期可能需要调整
            worldState.update("C20200901000001",origindata);
            worldState.sync();

            byte[] data = worldState.getAsBytes("C20200901000001");
            //Class clazz = ByteClassLoader.getClass(data,contractName);
            Class clazz = FileClassLoader.getClass(contractDir+"Add.class",contractName);
            System.out.println(clazz);
            Contract cobj = (Contract) clazz.newInstance();
            Map<String, DataUnit> arg = new HashMap<>();
            arg.put("KEY",new DataUnit("key"));
            arg.put("VALUE_1",new DataUnit(654));
            arg.put("VALUE_2",new DataUnit(234));
            cobj.initParam(arg);
            cobj.run(worldState);
            System.out.println(worldState.get("key"));


            ContractAccount contractAccount = new ContractAccount();

        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
