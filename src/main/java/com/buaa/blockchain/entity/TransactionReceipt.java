package com.buaa.blockchain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/8
 * @since JDK1.8
 */
@Getter
@Setter
@RequiredArgsConstructor
public class TransactionReceipt {
    private String receipt_hash;
    private String tx_hash;
    private Integer tx_sequence;
    private String block_hash;
    private Long height;
    private String to_address;

    private String logs;
    private String exec_result;
    private String error;

    private Transaction transaction;
}
