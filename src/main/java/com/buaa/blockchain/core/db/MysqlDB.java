package com.buaa.blockchain.core.db;

import com.buaa.blockchain.entity.*;
import com.buaa.blockchain.entity.mapper.BlockMapper;
import com.buaa.blockchain.entity.mapper.ContractAccountMapper;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import com.buaa.blockchain.entity.mapper.UserAccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

import java.math.BigInteger;
import java.util.List;

/**
 * MysqlDB
 * 这里的MysqlDB主要用于将StateDB的数据进行同步备份
 * StateDB是主要的区块链数据提供方，但是Mysql之类的关系数据库有着可视化的优势
 *
 * @author hitty
 *
 * */
@Slf4j
@MapperScan(basePackages ="com.buaa.blockchain.entity.mapper")
@ComponentScan(basePackages = "com.buaa.blockchain.*")
public class MysqlDB implements DB{
    /* Block的持久化 */
    final BlockMapper blockMapper;
    /* Transaction的持久化 */
    final TransactionMapper transactionMapper;
    /* UserAccount的持久化 */
    final UserAccountMapper userAccountMapper;
    /* ContractAccount的持久化 */
    final ContractAccountMapper contractAccountMapper;

    /**
     * 初始化的时候需要添加 @Autowired
     * */
    public MysqlDB(BlockMapper blockMapper, TransactionMapper transactionMapper, UserAccountMapper userAccountMapper,
                   ContractAccountMapper contractAccountMapper){
        this.blockMapper = blockMapper;
        this.transactionMapper = transactionMapper;
        this.userAccountMapper = userAccountMapper;
        this.contractAccountMapper = contractAccountMapper;
    }

    @Override
    public void insertUserAccount(UserAccount userAccount) {
        userAccountMapper.insertUserAccount(userAccount);
    }

    @Override
    public UserAccount findUserAccountByUserName(String userName) {
        return userAccountMapper.findUserAccountByName(userName);
    }

    @Override
    public void addBalance(String userName, BigInteger value) {
        UserAccount userAccount = this.userAccountMapper.findUserAccountByName(userName);
        userAccount.addBalance(value);
        userAccountMapper.updateBalance(userAccount.getBalance(), userName);
    }

    @Override
    public void insertBlock(Block block) {
        blockMapper.insertBlock(block);
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
    public long findBlockNum(String hash) {
        return blockMapper.findBlockNum(hash);
    }

    @Override
    public String findHashByHeight(long height) {
        return blockMapper.findHashByHeight(height);
    }

    @Override
    public long findMaxHeight() {
        return blockMapper.findMaxHeight();
    }

    @Override
    public String findStateRootByHeight(int height) {
        return blockMapper.findStatRoot(height);
    }

    @Override
    public void insertTimes(Times times) {
        blockMapper.insertTimes(times);
    }

    @Override
    public int insertTransaction(Transaction transaction) {
        return transactionMapper.insertTransaction(transaction);
    }

    @Override
    public List<Transaction> findTransByBlockHash(String hash) {
        return transactionMapper.findTransByBlockHash(hash);
    }

    @Override
    public Transaction findTransByHash(String tranHash) {
        return transactionMapper.findTransByHash(tranHash);
    }
}
