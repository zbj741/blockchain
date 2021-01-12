package com.buaa.blockchain.api;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.TransactionReceipt;
import com.buaa.blockchain.entity.mapper.BlockMapper;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import com.buaa.blockchain.entity.mapper.TransactionReceiptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockchainApiImpl implements BlockchainApi{

    final private BlockMapper blockMapper;
    final private TransactionMapper transactionMapper;
    final private TransactionReceiptMapper transactionReceiptMapper;

    @Autowired
    public BlockchainApiImpl(BlockMapper blockMapper, TransactionMapper transactionMapper, TransactionReceiptMapper transactionReceiptMapper) {
        this.blockMapper = blockMapper;
        this.transactionMapper = transactionMapper;
        this.transactionReceiptMapper = transactionReceiptMapper;
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
}
