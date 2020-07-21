package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.entity.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

@ComponentScan(basePackages = "com.buaa.blockchain.*")
@RestController
@Scope("prototype")
@RequestMapping("/data")
public class DataController {

    private final BlockchainApi blockchainApi;

    public DataController(BlockchainApi blockchainApi) {
        this.blockchainApi = blockchainApi;
    }

    @GetMapping(value = "/test")
    public String testController(){
        return "Test Done!";
    }

    @PostMapping(value = "/findBlockByHeight")
    public Block findBlockByHeight(@RequestParam(value = "height") int height){
        return blockchainApi.findBlockByHeight(height);
    }

}
