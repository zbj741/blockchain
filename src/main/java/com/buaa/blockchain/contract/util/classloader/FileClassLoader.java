package com.buaa.blockchain.contract.util.classloader;


import java.nio.file.Files;
import java.nio.file.Paths;



/**
 * 自定义的类加载器，从指定的路径加载.class文件到JVM中
 *
 * TODO: 使用ASM框架重写
 *
 * @author hitty
 * */
public class FileClassLoader extends ClassLoader {
    // 被动态加载的class路径
    private String path;

    /**
     * 按照参数返回class-
     * @param filePath class文件完整路径
     * @param className class代码的全限定类名
     * @return 对应class文件生成的Class对象
     * */
    public static synchronized Class getClass(String filePath,String className){
        FileClassLoader fileClassLoader = new FileClassLoader(filePath);
        Class clazz = null;
        try {
            clazz = fileClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            // TODO 未找到class
            clazz = null;
            e.printStackTrace();
        }finally {
            return clazz;
        }

    }



    private FileClassLoader(String path) {
        this.path = path;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] result = getClass(name);
            if (result == null) {
                throw new ClassNotFoundException();
            } else {
                return defineClass(name, result, 0, result.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getClass(String name) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}