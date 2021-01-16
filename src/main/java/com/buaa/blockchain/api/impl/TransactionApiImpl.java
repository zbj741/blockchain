package com.buaa.blockchain.api.impl;

import com.buaa.blockchain.api.TransactionApi;
import com.buaa.blockchain.entity.TransNumInfo;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionApiImpl implements TransactionApi {
    private final TransactionMapper transactionMapper;

    @Override
    public Transaction findTranByHash(String hash) {
        return transactionMapper.findTransByHash(hash);
    }

    @Override
    public List<Transaction> findTransByBlockHash(String hash) {
        return transactionMapper.findTransByBlockHash(hash);
    }

    @Override
    public List<TransNumInfo> getTransDayInfo(String startdate) {
        return transactionMapper.getTransDayInfo(startdate);
    }

    @Override
    public List<TransNumInfo> getTransMonInfo(String startdate) {
        return transactionMapper.getTransMonInfo(startdate);
    }

    @Override
    public List<TransNumInfo> getTransYearInfo(String startdate) {
        return transactionMapper.getTransYearInfo(startdate);
    }

    @Override
    public int findMaxSeq() { return transactionMapper.findMaxSeq(); }

    @Override
    public Transaction findTranBySeq(int seq) {
        return transactionMapper.findTranBySeq(seq);
    }

    @Override
    public List<Transaction> findPageTrans(int page_index, int page_size) {
        int offset = (page_index-1)*page_size <0? 0 : (page_index-1)*page_size;
        int count = page_size;
        return transactionMapper.findPageTrans(offset,count);
    }

}
