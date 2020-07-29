package com.buaa.blockchain.api;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.mapper.BlockMapper;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockchainApiImpl implements BlockchainApi{

    final
    private BlockMapper blockMapper;

    final
    private TransactionMapper transactionMapper;

    @Autowired
    public BlockchainApiImpl(BlockMapper blockMapper, TransactionMapper transactionMapper) {
        this.blockMapper = blockMapper;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public Boolean syncBlocks(List<Block> blockList, String address) {

        return null;
    }

    @Override
    public Block findBlockByHash(String hash) {
        return blockMapper.findBlockByHash(hash);
    }

    @Override
    public Block findBlockByHeight(int height) {
        return blockMapper.findBlockByHeight(height);
    }

    @Override
    public List<Block> listBlock() {
        return null;
    }

    @Override
    public List<Block> listBlockByHeight(int min, int max) {
        return null;
    }

    @Override
    public Transaction findTxByHash(String hash) {
        return null;
    }

    @Override
    public Transaction findTxByBlockHeight(int height) {
        return null;
    }

    @Override
    public Transaction findTxByBlockHash(String bhash) {
        return null;
    }

    @Override
    public List<Transaction> listTx() {
        return null;
    }

    @Override
    public List<Transaction> listTxByStartTime() {
        return null;
    }
}
