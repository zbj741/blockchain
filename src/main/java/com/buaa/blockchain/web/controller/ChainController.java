package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.txpool.TxPool;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Scope("prototype")
@RequestMapping("/chain")
public class ChainController {

    private final BlockchainApi blockchainApi;
    private TxPool txPool;

    @Autowired
    public ChainController(BlockchainApi blockchainApi, TxPool txPool) {
        this.blockchainApi = blockchainApi;
        this.txPool = txPool;
    }

    @GetMapping(value = "/")
    public ResponseEntity testController(){
        Map result = Maps.newHashMap();
        result.put("application", "BUAA CHAIN");
        result.put("version", "v1.0");
        return new ResponseEntity(result, HttpStatus.OK);
    }

}
