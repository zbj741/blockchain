package com.buaa.blockchain.contract.util.classloader;


import com.buaa.blockchain.contract.WorldState;

import java.io.File;
import java.io.FileInputStream;

public class ByteClassLoader extends ClassLoader{
    private byte[] byteInput;
    /**
     * 按照参数返回class
     * @param input class文件完整路径
     * @param className class代码的全限定类名
     * @return 对应class文件生成的Class对象
     * */
    public static synchronized Class getClass(byte[] input,String className){
        ByteClassLoader byteClassLoader = new ByteClassLoader(input);
        Class clazz = null;
        try {
            clazz = byteClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            // TODO 未找到class
            clazz = null;
            e.printStackTrace();
        }finally {
            return clazz;
        }

    }

    private ByteClassLoader(byte[] input) {
        this.byteInput = input;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] classBytes = this.byteInput;
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }


    public static void main(String[] args) {
        String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
        String contractName = "com.buaa.blockchain.contract.develop.ContractDemo";
        File fileln = new File (contractDir+"ContractDemo.class"); //打开源文件
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
            Class clazz = ByteClassLoader.getClass(data,contractName);
            System.out.println(clazz);

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

}
