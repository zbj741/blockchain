package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.crypto.CryptoSuite;
import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private ChainConfig chainConfig;

    @PostMapping("/new")
    public ResponseEntity<Map> newAccount(){
        CryptoSuite cryptoSuite = new CryptoSuite(chainConfig.getCryptoType());
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair();

        Map rtnMap = Maps.newHashMap();
        rtnMap.put("address", keyPair.getAddress());
        rtnMap.put("private_key", keyPair.getHexPrivateKey());
       return new ResponseEntity<>(rtnMap, HttpStatus.OK);
    }
}
