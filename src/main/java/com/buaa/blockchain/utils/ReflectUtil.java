package com.buaa.blockchain.utils;

import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/23
 * @since JDK1.8
 */
public class ReflectUtil {
    private ByteClassLoader byteClassLoader = new ByteClassLoader(ReflectUtil.class.getClassLoader());

    private static class SingletonHelper {
        private static final ReflectUtil INSTANCE = new ReflectUtil();
    }

    public static ReflectUtil getInstance(){
        return SingletonHelper.INSTANCE;
    }

    public Class loadClass(String className, byte[] code){
        if(!byteClassLoader.isLoad(className)){
            byteClassLoader.loadDataInBytes(code, className);
        }
        try {
            return byteClassLoader.loadClass(className);
        } catch (ClassNotFoundException classNotFoundException) {
            classNotFoundException.printStackTrace();
            return null;
        }
    }

    public Object newInstance(Class objClass, Class paramClass, Object param){
        Constructor c;
        try {
            c = objClass.getConstructor(paramClass);
        } catch (NoSuchMethodException noSuchMethodException) {
            noSuchMethodException.printStackTrace();
            return null;
        }
        try {
            return c.newInstance(param);
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public Object invoke(Class classObj, Object objInstance, String methodName, Object... param) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method defineMethod = MethodUtil.getMethod(classObj, methodName, param.length);

        Class<?>[] parameters = defineMethod.getParameterTypes();
        Object[] invokeObjs = new Object[param.length];
        for (int i = 0; i < param.length; i++) {
            String paramVal = String.valueOf(param[i]);
            if(Boolean.class.isAssignableFrom(parameters[i]) || boolean.class.isAssignableFrom(parameters[i])){
                invokeObjs[i] = Boolean.valueOf(paramVal);
            }else if(Integer.class.isAssignableFrom(parameters[i]) || int.class.isAssignableFrom(parameters[i])){
                invokeObjs[i] = Integer.valueOf(paramVal);
            }else if(Long.class.isAssignableFrom(parameters[i]) || long.class.isAssignableFrom(parameters[i])){
                invokeObjs[i] = Long.valueOf(paramVal);
            }else if(Double.class.isAssignableFrom(parameters[i]) || double.class.isAssignableFrom(parameters[i])){
                invokeObjs[i] = Double.valueOf(paramVal);
            }else if(BigDecimal.class.isAssignableFrom(parameters[i])){
                invokeObjs[i] = new BigDecimal(paramVal);
            }else{
                invokeObjs[i] = paramVal;
            }
        }
        return invoke(defineMethod, objInstance, invokeObjs);
    }

    private Object invoke(Method method, Object bean, Object... args) throws InvocationTargetException, IllegalAccessException {
        if (method.getParameters().length == 0) {
            return method.invoke(bean);
        } else {
            return method.invoke(bean,args);
        }
    }

}
