package com.buaa.blockchain.utils;

import java.lang.reflect.Method;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/23
 * @since JDK1.8
 */
public class MethodUtil {

    public static Method getMethod(Class<?> controller, String methodName) throws NoSuchMethodException {
        Method[] methods = controller.getDeclaredMethods();
        for(Method method : methods){
            if(method.getName().equals(methodName)){
                return method;
            }
        }
        throw new NoSuchMethodException();
    }
}
