package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.entity.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope("prototype")
@RequestMapping("/block")
public class BlockController {

    private final BlockchainApi blockchainApi;

    @Autowired
    public BlockController(BlockchainApi blockchainApi) {
        this.blockchainApi = blockchainApi;
    }

    @GetMapping(value = "/find_by_height")
    public Block findBlockByHeight(@RequestParam(value = "height") long height){
        return blockchainApi.findBlockByHeight(height);
    }

    @GetMapping(value = "/find_by_hash")
    public Block findBlockByHeight(@RequestParam(value = "hash") String hash){
        return blockchainApi.findBlockByHash(hash);
    }

    @GetMapping("/find_last_block")
    public Block findLastBlock(){
        return blockchainApi.findLastBlock();
    }
}
