package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.BlockNumInfo;
import com.buaa.blockchain.utils.ResultMsg;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Scope("prototype")
@RequestMapping("/block")
@RequiredArgsConstructor
public class BlockController {
    private final BlockchainApi blockchainApi;

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

    @GetMapping(value = "/findBlockByHeight")
    public ResultMsg findBlockByHeight(@RequestParam(value = "height") int height){
        ResultMsg result = new ResultMsg();
        Block block = blockchainApi.findBlockByHeight(height);
        result.setCode(200);
        result.setSuccess(true);
        result.setData(block);
        result.setMsg("区块信息");
        return result;
    }

    /**
     * 根据输入hash查找相应block
     * @param hash
     * @return
     */
    @GetMapping(value = "/findBlockByHash")
    public ResultMsg findBlockByHash(@RequestParam(value = "hash") String hash){
        ResultMsg result = new ResultMsg();
        Block block = blockchainApi.findBlockByHash(hash);
        result.setCode(200);
        result.setSuccess(true);
        result.setData(block);
        result.setMsg("区块信息");
        return result;
    }

    /**
     * 根据输入prehash查找相应block
     * @param prehash
     * @return
     */
    @GetMapping(value = "/findBlockByPreHash")
    public ResultMsg findBlockByPreHash(@RequestParam(value = "prehash") String prehash){
        ResultMsg result = new ResultMsg();
        Block block = blockchainApi.findBlockByHash(prehash);
        result.setCode(200);
        result.setSuccess(true);
        result.setData(block);
        result.setMsg("区块信息");
        return result;
    }
    /**
     *  获取当前做块最大高度
     * @return
     */
    @GetMapping(value = "/findMaxHeight")
    public ResultMsg findMaxHeight(){
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(blockchainApi.findMaxHeight());
        result.setMsg("区块最大高度");
        return result;
    }
    /**
     * 获取整个blocklist
     * @return
     */
    @GetMapping(value = "/getBlocklist")
    public ResultMsg getBlocklist() {
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(blockchainApi.getBlocklist());
        result.setMsg("区块列表");
        return result;
    }
    /**
     * 获取当前最新区块hash
     * @return
     */
    @GetMapping(value = "/getNowHash")
    public ResultMsg getNowHash() {
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(blockchainApi.getNowHash());
        result.setMsg("最新区块hash");
        return result;
    }

    /**
     * 根据height范围查找相应的区块
     * @param start
     * @param end
     * @return
     */
    @GetMapping(value = "/findBlocks")
    public ResultMsg findBlocks(@RequestParam(value = "start") int start,
                                @RequestParam(value = "end") int end) {
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(blockchainApi.findBlocks(start,end));
        result.setMsg("区块列表");
        return result;
    }

    @GetMapping(value = "/getBlockNumByTxRange")
    public List<BlockNumInfo> getBlockNumByTxRange(){
        //todo 重构 ResultMsg以及逻辑方面
        List<BlockNumInfo> getnums = new ArrayList<>();
        int irange = 50;
        for(int i=1000;i<=1150;i+=irange){
            int rangeup = i+irange;
            String range = i+"-"+rangeup;
            Long num = Long.valueOf(blockchainApi.getBlockNumByTxRange(i,i+irange));
            BlockNumInfo tmp = new BlockNumInfo();
            tmp.setBlock_num(num);
            tmp.setTxlength_range(range);
            getnums.add(tmp);
        }
        return getnums;
    }

    @GetMapping(value = "/getBlockNumBySign")
    public List<BlockNumInfo> getBlockNumBySign(){
        List<BlockNumInfo> rtnList = new ArrayList<>();

        List<Map<String, Object>> itemGroup = blockchainApi.countBlockNumGroupBySign();
        for(Map<String, Object> item : itemGroup){
            BlockNumInfo tmp = new BlockNumInfo();
            tmp.setBlock_num((Long)item.get("block_num"));
            tmp.setSign_node((String)item.get("sign"));
            rtnList.add(tmp);
        }
        return rtnList;
    }

    @GetMapping(value = "/findPageBlocks")
    public ResultMsg findPageBlocks(@RequestParam(value = "page_index") int page_index,
                                    @RequestParam(value = "page_size") int page_size){
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(blockchainApi.findPageBlocks(page_index,page_size));
        result.setMsg("第"+page_index+"页区块信息");
        return result;
    }

}
