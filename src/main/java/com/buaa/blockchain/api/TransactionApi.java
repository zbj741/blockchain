package com.buaa.blockchain.api;

import com.buaa.blockchain.entity.TransNumInfo;
import com.buaa.blockchain.entity.Transaction;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/15
 * @since JDK1.8
 */
public interface TransactionApi {
    Transaction findTranByHash(String hash);

    List<Transaction> findTransByBlockHash(String hash);

    List<TransNumInfo> getTransDayInfo(String startdate);

    List<TransNumInfo> getTransMonInfo(String startdate);

    List<TransNumInfo> getTransYearInfo(String startdate);

    int findMaxSeq();

    Transaction findTranBySeq(int seq);

    PageInfo findPageTrans(int page_index, int page_size);

}
