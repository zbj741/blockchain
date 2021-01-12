package com.buaa.blockchain.entity.mapper;

import com.buaa.blockchain.entity.TransactionReceipt;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/9
 * @since JDK1.8
 */
@Repository
public interface TransactionReceiptMapper {

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    @Insert("INSERT INTO transaction_receipt (" +
                " receipt_hash, tx_hash, block_hash, height, to_address, tx_sequence, logs, exec_result, error " +
            ") VALUES (" +
                " #{receipt_hash}, #{tx_hash}, #{block_hash}, #{height}, #{to_address}, #{tx_sequence}, #{logs}, #{exec_result}, #{error} " +
            ")")
    int insert(TransactionReceipt receipt);

    @Select("SELECT " +
                " receipt_hash, tx_hash, block_hash, height, to_address, tx_sequence, logs, exec_result, error " +
            " FROM " +
                " transaction_receipt" +
            " WHERE " +
            "   height = #{height}")
    List<TransactionReceipt> findByHeight(long height);

    @Select("SELECT * FROM " +
            "   transaction_receipt " +
            "where " +
            "   height between #{fromBlockNum} and #{toBlockNum} " +
            "ORDER BY height, tx_sequence")
    List<TransactionReceipt> findList(long fromBlockNum, long toBlockNum);
}
