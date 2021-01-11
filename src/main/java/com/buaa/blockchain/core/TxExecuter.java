package com.buaa.blockchain.core;

import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.component.ContractException;
import com.buaa.blockchain.contract.component.IContract;
import com.buaa.blockchain.contract.model.CallMethod;
import com.buaa.blockchain.crypto.CryptoSuite;
import com.buaa.blockchain.crypto.HashUtil;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.TransactionReceipt;
import com.buaa.blockchain.entity.UserAccount;
import com.buaa.blockchain.utils.ByteUtil;
import com.buaa.blockchain.utils.ReflectUtil;
import com.buaa.blockchain.vm.utils.ByteArrayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
    private ChainConfig chainConfig;
    private WorldState worldState;

    public TxExecuter(ChainConfig chainConfig, WorldState worldState){
       this.chainConfig = chainConfig;
       this.worldState = worldState;
    }

    public List<TransactionReceipt> batchExecute(List<Transaction> transactionList) {
        log.info("beforeBatchExecute(): start to execute transaction list, size=" + transactionList.size());
        List<TransactionReceipt> receipts = Lists.newArrayList();
        for (int i = 0; i < transactionList.size(); i++) {
            final Transaction tx = transactionList.get(i);

            TransactionReceipt result = execute(tx);
            result.setBlock_hash(tx.getBlock_hash());
            result.setTx_hash(tx.getTran_hash());
            result.setTransaction(tx);
            result.setTx_sequence(i);

            receipts.add(result);
        }
        log.info("afterBatchExecute(): execute end.");
        return receipts;
    }

    public TransactionReceipt execute(Transaction transaction){
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setReceipt_hash(String.valueOf(HashUtil.randomHash()));

        if(transaction.getTo() == null) {
            log.info("deploy contract......");
            // 1. decode the data value (contractName and contractCode)
            byte[] txData = transaction.getData();
            String contractName = new String(ByteArrayUtil.stripLeadingZeroes(ByteUtil.parseBytes(txData, 0, 32)));
            byte[] codeBytes = ByteUtil.parseBytes(txData, 32, txData.length-32);

            // 2. try to load the contract code and instance the contract
            Map storage = new HashMap();
            Class classZ = ReflectUtil.getInstance().loadClass(contractName, codeBytes);
            ReflectUtil.getInstance().newInstance(classZ, Map.class,  storage);

            // 3. create the contract account and linked data(code/storage)
            // 3.1 create address
            CryptoSuite cryptoSuite = new CryptoSuite(chainConfig.getCryptoType());
            String contractAddress = cryptoSuite.createKeyPair().getAddress();
            UserAccount userAccount = new UserAccount();
            // 3.2 store the contract code
            byte[] code_hash = HashUtil.sha256(codeBytes);
            worldState.update(new String(code_hash), codeBytes);
            userAccount.setCodeHash(code_hash);
            log.debug("code_hash: {}", new String(code_hash));
            //3.3 set the contract name
            userAccount.setContractName(contractName);
            log.debug("contract name: {}", userAccount.getContractName());
            //3.4 store the contract storage
            final byte[] storageHash = HashUtil.randomHash();
            try {
                String data_state = new ObjectMapper().writeValueAsString(storage);
                userAccount.setStorageHash(storageHash);
                worldState.update(new String(storageHash), data_state);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            log.debug("storage hash: {}", new String(storageHash));
            //3.5 store the contract user to world state.
            worldState.createAccount(contractAddress, userAccount);
            log.info("contract address at: {} ", contractAddress);
            log.debug("the contract meta: {}", userAccount.toString());
        } else {
            UserAccount userAccount = worldState.getUser(new String(transaction.getTo()));
            if(userAccount!=null && userAccount.isContractAccount()){
                log.debug("invoke the contract >> {}", userAccount.getContractName());
                CallMethod callMethod;
                try {
                    callMethod = new ObjectMapper().readValue(new String(transaction.getData()), CallMethod.class);
                } catch (JsonProcessingException e) {
                    log.error("instance CallMethod occur error. {}", transaction.getData() );
                    throw new IllegalArgumentException("Calling contract parameters incorrectly");
                }

                List contractLogList = new ArrayList();
                Map storage = this.worldState.getContractStorage(new String(transaction.getTo()));
                try {
                    log.debug("load the contract storage data, {}", new ObjectMapper().writeValueAsString(storage));
                } catch (JsonProcessingException e) {
                    log.error("failed to load the contract storage data. {}", storage);
                }

                boolean isExecuteSucc = false;
                byte[] code = this.worldState.getCode(new String(transaction.getTo()));
                Class classZ = ReflectUtil.getInstance().loadClass(userAccount.getContractName(), code);
                try {
                    Object obj = ReflectUtil.getInstance().newInstance(classZ, Map.class,  storage);

                    if(IContract.class.isAssignableFrom(obj.getClass())){
                        Field f1 = classZ.getSuperclass().getDeclaredField("LOGS");
                        f1.setAccessible(true);
                        f1.set(obj, contractLogList);
                    }

                    log.debug("invoke method: {}, {}", callMethod.getMethod(), callMethod.getParams());
                    Object execResult = ReflectUtil.getInstance().invoke(classZ, obj, callMethod.getMethod(), callMethod.getParams());
                    receipt.setLogs(new ObjectMapper().writeValueAsString(contractLogList));
                    receipt.setExec_result(new ObjectMapper().writeValueAsString(execResult));
                    isExecuteSucc = true;
                }  catch (Exception e){
                    receipt.setError(e.getMessage());
                    if(e.getCause().getClass().isAssignableFrom(ContractException.class)){
                        throw new ContractException(e.getCause().getMessage());
                    }
                    e.printStackTrace();
                }

                if(isExecuteSucc){
                    final byte[] storageHash = HashUtil.randomHash();
                    try {
                        String data_state = new ObjectMapper().writeValueAsString(storage);
                        worldState.update(new String(storageHash), data_state);
                        worldState.refreshStorage(new String(transaction.getTo()), storageHash);
                    } catch (Exception e) {
                        receipt.setError(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }else{
                worldState.addBalance(new String(transaction.getFrom()), transaction.getValue().negate());
                worldState.addBalance(new String(transaction.getTo()), transaction.getValue());
                log.debug("transfer: {} => {}, value: {}", new String(transaction.getFrom()), new String(transaction.getTo()), transaction.getValue());
            }
        }

        try {
            worldState.update(receipt.getReceipt_hash(), new ObjectMapper().writeValueAsString(receipt));
        } catch (JsonProcessingException e) {
           e.printStackTrace();
        }
        worldState.sync();
        return receipt;
    }
}
