package com.buaa.blockchain.entity.mapper;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Times;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface BlockMapper {

    @Select("SELECT pre_hash,hash,merkle_root,state_root,pre_state_root,height,sign,timestamp,extra,version,tx_length from block where hash = #{hash}")
    Block findBlockByHash(String hash);

    @Select("SELECT pre_hash,hash,merkle_root,state_root,pre_state_root,height,sign,timestamp,extra,version,tx_length from block where height = #{height}")
    Block findBlockByHeight(long height);

    @Select("SELECT pre_hash,hash,merkle_root,state_root,pre_state_root,height,sign,timestamp,extra,version,tx_length from block order by height desc limit 1")
    Block findLastBlock();

    @Select("SELECT hash from block where pre_hash = #{pre_hash}")
    Block findBlockByPreHash(String pre_hash);

    @Select("SELECT count(1) as num from block where hash = #{hash}")
    Long findBlockNum(String hash);

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Insert("INSERT INTO block ( pre_hash, hash, merkle_root, state_root,pre_state_root, height, sign, timestamp,version,extra,tx_length)"
            + " VALUES"
            + " (#{pre_hash}, #{hash}, #{merkle_root}, #{state_root}, #{pre_state_root},#{height},#{sign},#{timestamp},#{version},#{extra},#{tx_length})")
    int insertBlock(Block Block);

    @Select("SELECT * from block")
    List<Block> findAll();

    @Select("select hash from block where height = (select max(height) from block)")
    String findMaxBlockHash();

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Select("select hash from block where height = #{height}")
    String findHashByHeight(long height);

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    @Select("select max(height) from block")
    Long findMaxHeight();

    @Select("select state_root from block where height = #{height}")
    String findStatRoot(long height);

    @Select("select pre_hash,hash,merkle_root,state_root,pre_state_root,height,sign,timestamp,extra,version,tx_length from block where height > #{heightBegin} and height < #{heightEnd} order by height desc")
    List<Block> findBlocks(@Param("heightBegin") int heightBegin, @Param("heightEnd") int heightEnd);

    @Insert("INSERT INTO times (block_hash,tx_length,startCompute,broadcast,blockReceived,sendVote,voteReceived,storeBlock,removeTrans,storeTrans,endTime)"
            + " VALUES"
            + " (#{block_hash},#{tx_length},#{startCompute},#{broadcast},#{blockReceived},#{sendVote},#{voteReceived},#{storeBlock},#{removeTrans},#{storeTrans},#{endTime})")
    void insertTimes(Times times);

    @Select("SELECT count(*) from block where tx_length >= #{low} and tx_length <= #{top}")
    int getBlockNumByTxlength(int low, int top);

    @Select("SELECT count(*) from block where sign = #{sign}")
    int getBlockNumBySign(String sign);

    @ResultType(Map.class)
    @Select("SELECT sign, COUNT(*) as block_num, sum(tx_length) as tx_num FROM block GROUP BY sign")
    List<Map<String, Object>> countBlockNumGroupBySign();

    @Select("SELECT * from block order by height desc limit #{offset},#{count}")
    List<Block> findPageBlocks(int offset, int count);
}
