package com.buaa.blockchain.txpool;

import com.buaa.blockchain.entity.Transaction;

import java.util.List;

public interface TxPool {
    Transaction get(String hash,String tran_hash);
    void put(String hash,String tran_hash,Transaction transaction);
    void delete(String hash,String tran_hash);
    Long size(String hash);
    List<Transaction> getList(String hash, int size);

    // 交易池中的交易标签
    String TXPOOL_LABEL_TRANSACTION = "TRANSACTION";
    String TXPOOL_LABEL_DEL_TRANSACTION = "DEL_TRANSACTION";
}
