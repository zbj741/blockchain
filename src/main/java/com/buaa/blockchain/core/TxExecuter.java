package com.buaa.blockchain.core;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.core.ContractManager;
import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.core.IContractManager;
import com.buaa.blockchain.entity.ContractCaller;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易执行器
 * 用于将交易数据解包，并且发放给ContractManager来执行
 *
 * @author hitty
 * */
@Slf4j
public class TxExecuter {
    private static TxExecuter instance = null;
    private BlockchainService bs = null;
    private IContractManager contractManager;
    /**
     * 全局单例
     * */
    public synchronized static TxExecuter getInstance(BlockchainService bs,State state){
        if(null == instance){
            instance = new TxExecuter(bs, state);
        }
        return instance;
    }
    private TxExecuter(BlockchainService bs, State state){
        // 初始化ContractManager
        this.contractManager = ContractManager.getInstance(bs,state);
    }

    public void baseExecute(List<Transaction> transactionList, WorldState worldState){
        log.info("baseExecute(): start to execute transaction list, size="+transactionList.size());
        for(int i = 0;i < transactionList.size();i++){
            baseSingleExecute(transactionList.get(i),worldState);
        }

    }
    /**
     * 执行单个交易
     * 交易的执行，需要修改worldState，也许需要写数据库
     * */
    private void baseSingleExecute(Transaction transaction,WorldState worldState){
        switch (transaction.getType()){
            case Transaction.TYPE_CALL:{
                // 调用智能合约
                try {
                    ContractCaller contractCaller = JsonUtil.objectMapper.readValue(transaction.getData(), ContractCaller.class);
                    contractManager.invokeContract(worldState,contractCaller.getContractName(),contractCaller.getArg(),transaction.getLargeData());
                } catch (Exception e) {
                    // TODO 处理调用智能合约异常
                    e.printStackTrace();
                } finally {
                    // 合约执行失败了也无法处理XD
                }
                break;
            }
            case Transaction.TYPE_TEST:{
                // 测试用，插入一条数据
                worldState.update(transaction.getTran_hash(),transaction.getTran_hash());
                break;
            }

        }



    }
}
