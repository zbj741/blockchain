package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.txpool.TxPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Scope("prototype")
@RequestMapping("/tx")
public class TransactionController {

    private final BlockchainApi blockchainApi;
    private TxPool txPool;

    @Autowired
    public TransactionController(BlockchainApi blockchainApi, TxPool txPool) {
        this.blockchainApi = blockchainApi;
        this.txPool = txPool;
    }

    @GetMapping(value = "/send")
    public ResponseEntity testController(){
        Transaction ts = Transaction.createDefaultTransaction();
        this.txPool.put(TxPool.TXPOOL_LABEL_TRANSACTION, ts.getTran_hash(), ts);
        return new ResponseEntity(ts, HttpStatus.OK);
    }

    @GetMapping(value = "/find_by_height")
    public List<Transaction> findTxByBlockHeight(@RequestParam(value = "height") long height){
        return blockchainApi.findTxByBlockHeight(height);
    }

    @GetMapping(value = "/find_by_hash")
    public List<Transaction> findBlockByHeight(@RequestParam(value = "hash") String hash){
        return blockchainApi.findTxByBlockHash(hash);
    }

}
