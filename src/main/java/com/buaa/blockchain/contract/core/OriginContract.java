package com.buaa.blockchain.contract.core;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.entity.ContractAccount;
import com.buaa.blockchain.entity.ContractEntrance;
import com.buaa.blockchain.entity.UserAccount;
import com.buaa.blockchain.entity.mapper.BlockMapper;
import com.buaa.blockchain.entity.mapper.ContractAccountMapper;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import com.buaa.blockchain.entity.mapper.UserAccountMapper;
import com.buaa.blockchain.utils.JsonUtil;
import com.buaa.blockchain.utils.SpringContextUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import static com.buaa.blockchain.entity.ContractEntrance.CONTRACT_ENTRANCE_KEY;

/**
 * 原生合约，每一个节点在启动时初始化的合约
 * 原生合约直接存在于源码中，不使用反射来调用，用于提高执行速度
 *
 * @author hitty
 * */

@Slf4j
public class OriginContract {
    // contract文件夹路径
    private static final String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    // BlockchainService的引用
    BlockchainService bs = null;
    // 原生合约提供的方法名
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String DEVCLASS = "devClass";
    public static final String DEVJAR = "devJar";
    public static final String ADDUSER = "addUser";
    public static final String TRANSFER = "transfer";
    private HashSet<String> functions;

    /* Block的持久化 */
    final BlockMapper blockMapper;
    /* Transaction的持久化 */
    final TransactionMapper transactionMapper;
    /* UserAccount的持久化 */
    final UserAccountMapper userAccountMapper;
    /* ContractAccount的持久化 */
    final ContractAccountMapper contractAccountMapper;


    public OriginContract(BlockchainService bs){
        this.bs = bs;
        this.functions = new HashSet<>();
        functions = new HashSet<>();
        functions.add(UPDATE);
        functions.add(DELETE);
        functions.add(DEVCLASS);
        functions.add(DEVJAR);
        functions.add(ADDUSER);
        functions.add(TRANSFER);

        blockMapper = (BlockMapper) SpringContextUtil.getBean(BlockMapper.class);
        transactionMapper = (TransactionMapper) SpringContextUtil.getBean(TransactionMapper.class);
        userAccountMapper = (UserAccountMapper) SpringContextUtil.getBean(UserAccountMapper.class);
        contractAccountMapper = (ContractAccountMapper) SpringContextUtil.getBean(ContractAccountMapper.class);
    }

    /**
     * 获取全部的原生合约方法名
     * */
    public HashSet<String> getAllOriMethods(){
        return functions;
    }

    /**
     * 调用入口
     * @param state statedb实例
     * @param function 方法名
     * @param args 参数
     * */
    public void invoke(State state,String function,Map<String, DataUnit> args,byte[] largeData){
        log.info("invoke(): "+function+", args="+args.toString());
        // 分发函数的调用
        switch (function){
            case UPDATE:{
                update(state,args);
                break;
            }
            case DELETE:{
                delete(state,args);
                break;
            }
            case DEVCLASS:{
                devClass(state,args);
                break;
            }
            case DEVJAR:{
                devJar(state,args,largeData);
                break;
            }
            case ADDUSER:{
                addUser(state,args);
                break;
            }
            case TRANSFER:{
                transfer(state,args);
                break;
            }
            default:{
                log.warn("invoke(): contract "+function+" not found in OriginContract!");
                break;
            }
        }
        return ;
    }
    /**
     * 简单插入
     * */
    private void update(State state, Map<String, DataUnit> args){
        String key = args.get("KEY").toString();
        String value = args.get("VALUE").getValue();
        state.update(key,value);
    }
    /**
     * 简单删除
     * */
    private void delete(State state, Map<String, DataUnit> args){
        String key = args.get("KEY").toString();
        state.delete(key);
    }
    /**
     * 建立普通用户账户
     * */
    private void addUser(State state, Map<String, DataUnit> args){
        String address = args.get("ADDRESS").getString();
        try {
            UserAccount userAccount = new UserAccount();
            state.update(address, JsonUtil.objectMapper.writeValueAsBytes(userAccount));
            userAccountMapper.insertUserAccount(userAccount);
        } catch (JsonProcessingException e) {
            // TODO 生成userAccount错误
            e.printStackTrace();
        }
    }
    /**
     * 简单转账（用户）
     * */
    private void transfer(State state, Map<String, DataUnit> args){
//        String ua1 = args.get("USERACCOUNT1").getString();
//        String ua2 = args.get("USERACCOUNT2").getString();
//        BigInteger t = args.get("AMOUNT").getBigInteger();
//        UserAccount userAccount1 = null;
//        UserAccount userAccount2 = null;
//        try {
//            userAccount1 = JsonUtil.objectMapper.readValue(state.get(ua1),UserAccount.class);
//            userAccount2 = JsonUtil.objectMapper.readValue(state.get(ua2),UserAccount.class);
//        } catch (JsonProcessingException e) {
//            // TODO 读取失败,转账停止
//            e.printStackTrace();
//        }
//        BigInteger pre1 = userAccount1.getBalance();
//        BigInteger pre2 = userAccount2.getBalance();
//        userAccount1.addBalance(t.negate());
//        userAccount2.addBalance(t);
//        log.info("transfer(): "+userAccount1.getUserName()+" transfer t="+t+" to "+userAccount2.getUserName()+"; "
//                +" [balance]:"+userAccount1.getUserName()+":"+pre1+"->"+userAccount1.getBalance()+", "+
//                userAccount2.getUserName()+":"+pre2+"->"+userAccount2.getBalance());
//        // 写回state
//        try{
//            state.update(userAccount1.getUserName(), JsonUtil.objectMapper.writeValueAsBytes(userAccount1));
//            state.update(userAccount2.getUserName(), JsonUtil.objectMapper.writeValueAsBytes(userAccount2));
//        }catch (JsonProcessingException e) {
//            // TODO 写回失败,转账停止
//            e.printStackTrace();
//        }
//        // 写回mysql
//        this.userAccountMapper.updateBalance(userAccount1.getUserName(),userAccount1.getBalance());
//        this.userAccountMapper.updateBalance(userAccount2.getUserName(),userAccount2.getBalance());
    }
    /**
     * 智能合约部署
     * */
    private void devClass(State state, Map<String, DataUnit> args){
        String contractName = args.get("CONTRACT_NAME").getString();
        byte[] classData = args.get("CONTRACT_BYTES").getByteArray();
        // 建立ContractAccount实例
        // TODO 暂时将智能合约用户的 key，id，name设为一样的，过后修改
        ContractAccount contractAccount = new ContractAccount(contractName,contractName,classData);
        contractAccount.setData("Class File");
        boolean done = false;
        // 将自己写入state
        try {
            state.update(contractAccount.getcKey(), JsonUtil.objectMapper.writeValueAsBytes(contractAccount));
            // 更新ContractEntrance
            ContractEntrance.getInstance().addContract(contractAccount);
            state.update(CONTRACT_ENTRANCE_KEY,JsonUtil.objectMapper.writeValueAsBytes(ContractEntrance.getInstance()));
            done = true;
            // 写入mysql
           // bs.insertContractAccount(contractAccount);

            // 写入contract文件夹
            File file = new File(contractDir+contractName+".class");
            FileOutputStream fileRes = new FileOutputStream (file);
            fileRes.write(classData);
            fileRes.close();
            file.setExecutable(true);
            file.setReadable(true);
            file.setWritable(true);


        } catch (JsonProcessingException e) {
            // 写入错误则删除
            e.printStackTrace();
            done = false;
        }catch (IOException e){
            // 写入文件错误 非主要问题 暂时不处理
            log.warn("devClass(): write class file failed!");
            e.printStackTrace();
        } finally {
            if(done){
                log.info("devClass(): develop contract "+contractName+" successfully.");
                // 写入contract文件夹

            }else{
                state.delete(contractAccount.getcKey());
                log.info("devClass(): develop contract "+contractName+" failed, remove data from state at key="+contractAccount.getcKey());
            }
        }

    }
    /**
     * 智能合约部署
     * */
    private void devJar(State state, Map<String, DataUnit> args,byte[] classData){
        String contractName = args.get("CONTRACT_NAME").getString();
        String type = args.get("CONTRACT_TYPE").getString();
        // 建立ContractAccount实例
        // TODO 暂时将智能合约用户的 key，id，name设为一样的，过后修改
        ContractAccount contractAccount = new ContractAccount(contractName,contractName,classData);
        contractAccount.setData(type);
        boolean done = false;
        try {
            // 写入contract文件夹
            contractAccount.saveAsJarFile();
            // loadJar
            contractAccount.loadJar();
            // 写入state
            state.update(contractAccount.getcKey(), JsonUtil.objectMapper.writeValueAsBytes(contractAccount));
            // 更新ContractEntrance
            ContractEntrance.getInstance().addContract(contractAccount);
            state.update(CONTRACT_ENTRANCE_KEY,JsonUtil.objectMapper.writeValueAsBytes(ContractEntrance.getInstance()));
            done = true;
            // 写入mysql
            //bs.insertContractAccount(contractAccount);
        } catch (JsonProcessingException e) {
            // 写入错误则删除
            e.printStackTrace();
            done = false;
        }catch (IOException e){
            // 写入文件错误 非主要问题 暂时不处理
            log.warn("devJar(): write class file failed!");
            e.printStackTrace();
        } finally {
            if(done){
                log.info("devJar(): develop contract "+contractName+" successfully.");
                return ;
            }else{
                state.delete(contractAccount.getcKey());
                log.info("devJar(): develop contract "+contractName+" failed, remove data from state at key="+contractAccount.getcKey());
            }
        }

    }

}
