package com.buaa.blockchain.annotation;

import java.lang.annotation.*;

/**
 * 读区块链数据的函数
 *
 * @author hitty
 * */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReadData {
}

