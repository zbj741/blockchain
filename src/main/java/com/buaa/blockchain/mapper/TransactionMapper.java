package com.buaa.blockchain.mapper;

import com.buaa.blockchain.entity.dao.TransactionSQLHelper;
import com.buaa.blockchain.entity.Transaction;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理Transaction的Mapper
 *
 * @author hitty
 *
 * */
@Repository
public interface TransactionMapper {

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @InsertProvider(type = TransactionSQLHelper.class, method = "insertAllTrans")
    public void insertAllTrans(List<Transaction> translist);
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Insert("INSERT INTO transaction ( block_hash,tran_hash, type, timestamp, sequence, sign,version,extra,data)"
            + " VALUES"
            + " (#{block_hash}, #{tran_hash}, #{type}, #{timestamp}, #{sequence},#{sign},#{version},#{extra},#{data})")
    public int insertTransaction(Transaction transaction);
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Select("select tran_hash,block_hash,type,timestamp,sequence,sign,version,extra,data,tranSeq from transaction where block_hash = #{blockHash} order by tranSeq")
    public ArrayList<Transaction> findTransByBlockHash(String blockHash);

    @Select("select tran_hash,block_hash,type,timestamp,sequence,sign,version,extra,data,tranSeq from transaction where tran_hash = #{tranHash}")
    public Transaction findTransByHash(String tranHash);





}
