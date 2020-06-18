package com.buaa.blockchain.entity.dao;

import com.buaa.blockchain.entity.Transaction;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;


/**
 * 将交易数据存入数据库中
 * 该过程发生在交易已完成执行，需要存入数据的阶段，可以独立使用线程来异步执行，防止阻塞等待时间
 *
 * @author hitty
 * */
public class TransactionSQLHelper {

    //将区块链中的交易存入数据库中
    public String insertAllTrans(Map map) {

        List<Transaction> trans = (List<Transaction>) map.get("list");
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO transaction ");
        sb.append("(tran_hash,block_hash,type,timestamp,sequence,sign,version,extra,tranSeq,data) ");
        sb.append("VALUES ");
        MessageFormat mf = new MessageFormat("(#'{'list[{0}].tran_hash},"+ "#'{'list[{0}].block_hash}, " + "#'{'list[{0}].type}, " + "#'{'list[{0}].timestamp}, "
                + "#'{'list[{0}].sequence}, " + "#'{'list[{0}].sign}, " + "#'{'list[{0}].version}, "
                + "#'{'list[{0}].extra}, " + "#'{'list[{0}].tranSeq} ," + "#'{'list[{0}].data} " + ")");
        for (int i = 0; i < trans.size(); i++) {
            sb.append(mf.format(new String[] { String.valueOf(i) }));
            if (i < trans.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}