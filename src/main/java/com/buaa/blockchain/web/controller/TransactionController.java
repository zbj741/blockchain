package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.crypto.CryptoSuite;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.txpool.TxPool;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@Scope("prototype")
@RequestMapping("/tx")
public class TransactionController {

    private final BlockchainApi blockchainApi;
    private TxPool txPool;

    @Autowired
    private ChainConfig chainConfig;

    @Autowired
    public TransactionController(BlockchainApi blockchainApi, TxPool txPool) {
        this.blockchainApi = blockchainApi;
        this.txPool = txPool;
    }

    @PostMapping(value = "/send")
    public ResponseEntity sendTransaction(@RequestBody() Transaction tx, @RequestParam(value ="sig") String sig){
        final StringBuilder sb = new StringBuilder("Transaction{");
        sb.append("data=").append(Arrays.toString(tx.getData()));
        sb.append(", from=").append(Arrays.toString(tx.getFrom()));
        sb.append(", to=").append(Arrays.toString(tx.getTo()));
        sb.append(", value=").append(tx.getValue());
        sb.append('}');
        CryptoSuite cs = new CryptoSuite(chainConfig.getCryptoType());
        System.out.println(sb.toString());
        System.out.println(cs.hash(sb.toString()));

//        if(!cs.verify(new String(tx.getFrom()), cs.hash(sb.toString()), sig)){
//            Map rtnMap = Maps.newHashMap();
//            rtnMap.put("status", 0);
//            rtnMap.put("message", "Signature verify failed");
//            return new ResponseEntity(rtnMap, HttpStatus.OK);
//        }
        this.txPool.put(TxPool.TXPOOL_LABEL_TRANSACTION, tx.getTran_hash(), tx);

        Map rtnMap = Maps.newHashMap();
        rtnMap.put("tx_hash", tx.getTran_hash());
        rtnMap.put("status", 1);
        return new ResponseEntity(rtnMap, HttpStatus.OK);
    }

    @GetMapping(value = "/find_by_blockhash")
    public List<Transaction> findTxByBlockHash(@RequestParam(value = "block_hash") String block_hash){
        return blockchainApi.findTxByBlockHash(block_hash);
    }

    @GetMapping(value = "/find_by_hash")
    public Transaction findByTxHash(@RequestParam(value = "hash") String hash){
        return blockchainApi.findTxByTxHash(hash);
    }

}
