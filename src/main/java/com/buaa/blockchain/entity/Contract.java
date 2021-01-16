package com.buaa.blockchain.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/16
 * @since JDK1.8
 */
@Getter
@Setter
public class Contract {
    private String name;    // 合约名称
    private String codeHash;    // 合约字节码
    private String address; // 合约地址
    private Date timestamp; // 创建合约时间
}
