package com.buaa.blockchain.core;

import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.component.ContractException;
import com.buaa.blockchain.contract.component.IContract;
import com.buaa.blockchain.contract.model.CallMethod;
import com.buaa.blockchain.crypto.HashUtil;
import com.buaa.blockchain.crypto.hash.Hash;
import com.buaa.blockchain.crypto.hash.Keccak256;
import com.buaa.blockchain.crypto.utils.ByteUtils;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.TransactionReceipt;
import com.buaa.blockchain.entity.UserAccount;
import com.buaa.blockchain.entity.mapper.ContractMapper;
import com.buaa.blockchain.utils.PackageUtil;
import com.buaa.blockchain.utils.ReflectUtil;
import com.buaa.blockchain.vm.utils.ByteArrayUtil;
import com.buaa.blockchain.vm.utils.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

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
    private ContractMapper contractMapper;

    public TxExecuter(ChainConfig chainConfig, WorldState worldState, ContractMapper contractMapper){
       this.chainConfig = chainConfig;
       this.worldState = worldState;
       this.contractMapper = contractMapper;
    }

    public List<TransactionReceipt> batchExecute(List<Transaction> transactionList) {
        log.info("beforeBatchExecute(): start to execute transaction list, size=" + transactionList.size());
        List<TransactionReceipt> receipts = Lists.newArrayList();
        for (int i = 0; i < transactionList.size(); i++) {
            final Transaction tx = transactionList.get(i);

            TransactionReceipt result = execute(tx);
            if(result == null){
                continue;
            }

            result.setBlock_hash(tx.getBlock_hash());
            result.setTx_hash(tx.getTran_hash());
            result.setTransaction(tx);
            result.setTx_sequence(i);
            if(tx.getTo() != null){
                result.setTo_address(new String(tx.getTo()));
            }

            receipts.add(result);
        }
        log.info("afterBatchExecute(): execute end.");
        return receipts;
    }

    public TransactionReceipt execute(Transaction transaction){
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setReceipt_hash(new String(createTxReceiptHash(transaction)));
        if(transaction.isCreateContract()) {
            log.info("deploy contract......");
            Class mainClass = null;
            String contractName = null;
            // 1. unpack the data and reload the bytes of multi classes to classloader (contractName and contractCode)
            final byte[] codeBytes = transaction.getData();
            List<byte[]> byteList = PackageUtil.unPack(codeBytes);
            for (byte[] byteClass : byteList){
                String className = new String(ByteArrayUtil.stripLeadingZeroes(ByteUtils.parseBytes(byteClass, 0, 32)));
                byte[] classBytes = ByteUtils.parseBytes(byteClass, 32, byteClass.length-32);
                Class classZ = ReflectUtil.getInstance().loadClass(className, classBytes);

                if(mainClass == null){
                    mainClass = classZ;
                    contractName = className;
                }
            }

            // 2. try to load the contract code and instance the contract
            Map storage = new HashMap();
            ReflectUtil.getInstance().newInstance(mainClass, Map.class, storage);

            // 3. create the contract account and linked data(code/storage)
            // 3.1 create address
//            CryptoSuite cryptoSuite = new CryptoSuite(chainConfig.getCryptoType());
            String contractAddress = new String(transaction.getTo());
            UserAccount userAccount = new UserAccount();
            // 3.2 store the contract code
            byte[] code_hash = HashUtil.sha256(codeBytes);
            worldState.update(new String(code_hash), codeBytes);
            userAccount.setCodeHash(code_hash);
            log.debug("\n\tcode_hash: {}", new String(code_hash));
            //3.3 set the contract name
            userAccount.setContractName(contractName);
            log.debug("\n\tcontract name: {}", userAccount.getContractName());
            //3.4 store the contract storage
            final byte[] storageHash = createStorageHash(transaction);
            try {
                String data_state = new ObjectMapper().writeValueAsString(storage);
                userAccount.setStorageHash(storageHash);
                worldState.update(HexUtil.toHexString(storageHash), data_state);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            log.debug("\n\tstorage hash: {}", HexUtil.toHexString(storageHash));
            //3.5 store the contract user to world state.
            worldState.createAccount(contractAddress, userAccount);
            log.info("\n\tcontract address at: {} ", contractAddress);
            log.debug("\n\tthe contract meta: {}", userAccount.toString());

            if(this.contractMapper != null){
                try {
                    this.contractMapper.insert(contractName, HexUtil.toHexString(code_hash), contractAddress, new Date());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("failed to save the contract, {}", contractName);
                }
            }
        } else {
            UserAccount userAccount = worldState.getUser(new String(transaction.getTo()));
            if(userAccount!=null && userAccount.isContractAccount()){
                log.debug("invoke the contract >> {}", userAccount.getContractName());
                CallMethod callMethod;
                try {
                    callMethod = new ObjectMapper().readValue(transaction.getData(), CallMethod.class);
                } catch (Exception e) {
                    log.error("instance CallMethod occur error. {}", transaction.getData() );
                    throw new IllegalArgumentException("Calling contract parameters incorrectly");
                }

                List contractLogList = new ArrayList();
                String err = "";
                Map storage = this.worldState.getContractStorage(new String(transaction.getTo()));
                try {
                    log.debug("load the contract storage data, {}", new ObjectMapper().writeValueAsString(storage));
                } catch (JsonProcessingException e) {
                    log.error("failed to load the contract storage data. {}", storage);
                }

                boolean isExecuteSucc = false;
                final byte[] codeBytes = this.worldState.getCode(new String(transaction.getTo()));
                try {
                    Class mainClass = null;
                    String contractName = null;
                    List<byte[]> byteList = PackageUtil.unPack(codeBytes);
                    for (byte[] byteClass : byteList){
                        String className = new String(ByteArrayUtil.stripLeadingZeroes(ByteUtils.parseBytes(byteClass, 0, 32)));
                        byte[] classBytes = ByteUtils.parseBytes(byteClass, 32, byteClass.length-32);
                        Class classZ = ReflectUtil.getInstance().loadClass(className, classBytes);

                        if(mainClass == null){
                            mainClass = classZ;
                            contractName = className;
                        }
                    }

                    Object obj = ReflectUtil.getInstance().newInstance(mainClass, Map.class, storage);

                    if(IContract.class.isAssignableFrom(obj.getClass())){
                        Field f1 = mainClass.getSuperclass().getDeclaredField("LOGS");
                        f1.setAccessible(true);
                        f1.set(obj, contractLogList);
                    }

                    log.debug("invoke method: {}, {}", callMethod.getMethod(), callMethod.getParams());
                    Object execResult = ReflectUtil.getInstance().invoke(mainClass, obj, callMethod.getMethod(), callMethod.getParams());
                    receipt.setLogs(new ObjectMapper().writeValueAsString(contractLogList));
                    receipt.setExec_result(new ObjectMapper().writeValueAsString(execResult));
                    isExecuteSucc = true;
                }  catch (Exception e){
                    e.printStackTrace();
                    if(e.getCause() !=null && e.getCause().getClass() != null && e.getCause().getClass().isAssignableFrom(ContractException.class)){
                        receipt.setError(e.getMessage());
                    }
                }

                if(isExecuteSucc){
                    final byte[] storageHash = createStorageHash(transaction);
                    try {
                        String data_state = new ObjectMapper().writeValueAsString(storage);
                        worldState.update(HexUtil.toHexString(storageHash), data_state);
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

        /*try {
            worldState.update(receipt.getReceipt_hash(), new ObjectMapper().writeValueAsString(receipt));
        } catch (JsonProcessingException e) {
           e.printStackTrace();
        }*/
        worldState.sync();
        return receipt;
    }

    private byte[] createTxHash(Transaction tx){
        Hash hash = new Keccak256();
        return hash.hash("Tx"+tx.toString()).substring(0, 32).getBytes();
    }

    private byte[] createStorageHash(Transaction tx){
        Hash hash = new Keccak256();
        return hash.hash("TxST"+tx.toString()).substring(0, 32).getBytes();
    }

    private byte[] createTxReceiptHash(Transaction tx){
        Hash hash = new Keccak256();
        return hash.hash("TxRE"+tx.toString()).substring(0, 32).getBytes();
    }
}
