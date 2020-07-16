package com.buaa.blockchain.annotation;

import java.lang.annotation.*;

/**
 * 写区块链数据的函数
 * 包括了写trie和mysql
 * 这种函数应该是排他运行的
 *
 * @author hitty
 * */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WriteData {
}
