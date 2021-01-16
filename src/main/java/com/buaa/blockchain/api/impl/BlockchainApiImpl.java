package com.buaa.blockchain.api.impl;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.TransactionReceipt;
import com.buaa.blockchain.entity.mapper.BlockMapper;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import com.buaa.blockchain.entity.mapper.TransactionReceiptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BlockchainApiImpl implements BlockchainApi {
    private final BlockMapper blockMapper;
    private final TransactionMapper transactionMapper;
    private final TransactionReceiptMapper transactionReceiptMapper;

    @Override
    public Boolean syncBlocks(List<Block> blockList, String address) {
        return null;
    }

    @Override
    public Block findBlockByHash(String hash) {
        return blockMapper.findBlockByHash(hash);
    }

    @Override
    public Block findBlockByHeight(long height) {
        return blockMapper.findBlockByHeight(height);
    }

    @Override
    public Block findLastBlock() {
        return blockMapper.findLastBlock();
    }

    @Override
    public Transaction findTxByTxHash(String tx_hash) {
        return transactionMapper.findTransByHash(tx_hash);
    }

    @Override
    public List<Transaction> findTxByBlockHash(String bhash) {
        return transactionMapper.findTransByBlockHash(bhash);
    }

    @Override
    public List<TransactionReceipt> findReceiptsByHeight(long height) {
        return transactionReceiptMapper.findByHeight(height);
    }

    @Override
    public Block findBlockByPreHash(String prehash) {
        return blockMapper.findBlockByPreHash(prehash);
    }

    @Override
    public List<Block> findBlocks(int start, int end) {
        return blockMapper.findBlocks(start, end);
    }

    @Override
    public Long findMaxHeight() {
        return blockMapper.findMaxHeight();
    }

    @Override
    public List<Block> getBlocklist() {
        return blockMapper.findAll();
    }

    @Override
    public String getNowHash() {
        return blockMapper.findMaxBlockHash();
    }

    @Override
    public int getBlockNumByTxRange(int low, int top) {
        return blockMapper.getBlockNumByTxlength(low, top);
    }

    @Override
    public int getBlockNumBySign(String sign) {
        return blockMapper.getBlockNumBySign(sign);
    }

    @Override
    public List<Map<String, Object>> countBlockNumGroupBySign(){
        return blockMapper.countBlockNumGroupBySign();
    }

    @Override
    public List<Block> findPageBlocks(int page_index, int page_size) {
        int offset = (page_index - 1) * page_size < 0 ? 0 : (page_index - 1) * page_size;
        int count = page_size;
        return blockMapper.findPageBlocks(offset, count);
    }

}
