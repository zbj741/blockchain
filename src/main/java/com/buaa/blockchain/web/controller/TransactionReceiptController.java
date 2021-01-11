package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.entity.TransactionReceipt;
import com.buaa.blockchain.entity.mapper.TransactionReceiptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/9
 * @since JDK1.8
 */
@RestController
@Scope("prototype")
@RequestMapping("/receipt")
public class TransactionReceiptController {

    @Autowired
    private final BlockchainApi blockchainApi;
    @Autowired
    private final TransactionReceiptMapper transactionReceiptMapper;

    public TransactionReceiptController(BlockchainApi blockchainApi, TransactionReceiptMapper transactionReceiptMapper) {
        this.transactionReceiptMapper = transactionReceiptMapper;
        this.blockchainApi = blockchainApi;
    }

    @GetMapping(value = "/list")
    public List<TransactionReceipt> findTxByBlockHash(@RequestParam(value = "height") long height) {
        return blockchainApi.findReceiptsByHeight(height);
    }

    @GetMapping(value = "/add")
    public ResponseEntity findTxByBlockHash(@RequestParam(value = "height") Long height) {
        TransactionReceipt tx = new TransactionReceipt();
        tx.setBlock_hash("111");
        tx.setTx_hash("222");
        tx.setTx_sequence(1);
        tx.setReceipt_hash("333");
        tx.setHeight(101l);

        tx.setLogs("log");
        tx.setExec_result("exec_result");
        tx.setError("error");
        transactionReceiptMapper.insert(tx);
        return ResponseEntity.ok().build();
    }
}
