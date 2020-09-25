package com.buaa.blockchain.contract.util.classloader;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ByteClassLoader extends ClassLoader{
    private byte[] byteInput;
    /**
     * 按照参数返回class
     * @param input class文件完整路径
     * @param className class代码的全限定类名
     * @return 对应class文件生成的Class对象
     * */
    public static synchronized Class getClass(byte[] input,String className) {
        ByteClassLoader byteClassLoader = new ByteClassLoader(input);
        System.out.println(ByteClassLoader.class.getCanonicalName());
        System.out.println(byteClassLoader.getParent().toString());

        Class clazz = null;
        try {
            System.out.println("before loadClass...");
            clazz = byteClassLoader.loadClass(className);
            System.out.println("after loadClass...");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            clazz = null;
        } catch (Exception e){
            System.out.println("return: "+clazz);
            e.printStackTrace();
        }

        return clazz;
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





}
