package com.buaa.blockchain.entity.mapper;

import com.buaa.blockchain.entity.TransNumInfo;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.dao.TransactionSQLHelper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
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

    @Insert("INSERT INTO transaction ( block_hash,tran_hash, type, timestamp, sequence, sign,version,extra,data,largeData,to_address,from_address,value)"
            + " VALUES"
            + " (#{block_hash}, #{tran_hash}, #{type}, #{timestamp}, #{sequence},#{sign},#{version},#{extra},#{data, typeHandler=org.apache.ibatis.type.ByteArrayTypeHandler},#{largeData},#{to_address},#{from_address},#{value})")
    public int insertTransaction(Transaction transaction);

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Select("select tran_hash,block_hash,type,timestamp,sequence,sign,version,extra,data,tranSeq from transaction where block_hash = #{blockHash} order by tranSeq")
    public ArrayList<Transaction> findTransByBlockHash(String blockHash);

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Select("select tran_hash,block_hash,type,timestamp,sequence,sign,version,extra,data,tranSeq,to_address,from_address,value from transaction where block_hash = #{blockHash} order by tranSeq")
    public ArrayList<Transaction> findTransByBlockHashSync(String blockHash);

    @Select("select tran_hash,block_hash,type,timestamp,sequence,sign,version,extra,data,tranSeq from transaction where tran_hash = #{tranHash}")
    public Transaction findTransByHash(String tranHash);

    @Select("select * from transaction where sequence = #{seq}")
    public Transaction findTranBySeq(int seq);

    @Select("select max(seq) from transaction for update")
    public int findMaxSeq();

    @Select("SELECT date_format(timestamp,'%Y-%m-%d') as date,COUNT(*) as num FROM transaction where date_format(timestamp,'%Y-%m-%d')>=#{startdate} GROUP BY date;")
    public List<TransNumInfo> getTransDayInfo(String startdate);

    @Select("SELECT date_format(timestamp,'%Y-%m') as date,COUNT(*) as num FROM transaction where date_format(timestamp,'%Y-%m')>=#{startdate} GROUP BY date;")
    public List<TransNumInfo> getTransMonInfo(String startdate);

    @Select("SELECT date_format(timestamp,'%Y') as date,COUNT(*) as num FROM transaction where date_format(timestamp,'%Y')>=#{startdate} GROUP BY date;")
    public List<TransNumInfo> getTransYearInfo(String startdate);

    @Select("SELECT * from transaction order by timestamp desc ")
    public List<Transaction> findPageTrans();


}
