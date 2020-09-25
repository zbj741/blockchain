package com.buaa.blockchain.test;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class LoadJarTest {
    static String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    static String contractName = "com.buaa.blockchain.contract.develop.Add";

    public static void main(String[] args) {
        LoadJar(null);
    }

    public static void LoadJar(State state){

        String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
        String contractName = "com.buaa.blockchain.contract.develop.ChangeBalance";
        String softPath = "file:"+contractDir+"ChangeBalance.jar";
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL(softPath)},Thread.currentThread().getContextClassLoader());
            Class demo = classLoader.loadClass(contractName);
            Contract object = (Contract) demo.newInstance();

            System.out.println(object.getName());
//            Map<String, DataUnit> arg = new HashMap<>();
//            arg.put("KEY",new DataUnit("key"));
//            arg.put("VALUE_1",new DataUnit(654));
//            arg.put("VALUE_2",new DataUnit(234));
//            object.initParam(arg);
//            object.run(state);
            System.out.println("EXECUTE JAR");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
