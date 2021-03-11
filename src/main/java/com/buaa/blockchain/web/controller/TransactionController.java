package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.api.TransactionApi;
import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.core.BlockchainServiceImpl;
import com.buaa.blockchain.crypto.HashUtil;
import com.buaa.blockchain.crypto.utils.Hex;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.BlockList;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.mapper.BlockMapper;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import com.buaa.blockchain.message.Message;
import com.buaa.blockchain.sdk.ChainSDK;
import com.buaa.blockchain.sdk.model.SignTransaction;
import com.buaa.blockchain.txpool.TxPool;
import com.buaa.blockchain.utils.JsonUtil;
import com.buaa.blockchain.utils.ResultMsg;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.buaa.blockchain.core.BlockchainService.CORE_MESSAGE_TOPIC_SYNC_END;

@Slf4j
@RestController
@Scope("prototype")
@RequestMapping("/tx")
@RequiredArgsConstructor
public class TransactionController {
    private final BlockchainApi blockchainApi;
    private final TxPool txPool;
    private final ChainConfig chainConfig;
    private final TransactionMapper transactionMapper;
    private final TransactionApi transactionApi;
    private final BlockchainServiceImpl blockchainServiceImpl ;
    private final BlockMapper blockMapper;

    @GetMapping(value = "/findTranByHash")
    public ResultMsg findTranByHash(@RequestParam(value = "tranhash") String tranhash) {
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(transactionApi.findTranByHash(tranhash));
        result.setMsg("交易信息");
        return result;
    }

    @GetMapping(value = "/findTransByBlockHash")
    public ResultMsg findTransByBlockHash(@RequestParam(value = "hash") String hash) {
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(transactionApi.findTransByBlockHash(hash));
        result.setMsg("交易信息");
        return result;
    }

    @GetMapping(value = "/getTransInfo")
    public ResultMsg getTransInfo(@RequestParam(value = "startdate") String startdate,
                                  @RequestParam(value = "span") String span)  {
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        if(span.equals("day")){
            result.setMsg("日交易");
            result.setData(transactionApi.getTransDayInfo(startdate));
        }
        else if(span.equals("month")){
            result.setMsg("月交易");
            result.setData(transactionApi.getTransMonInfo(startdate));
        }
        else if(span.equals("year")){
            result.setMsg("年交易");
            result.setData(transactionApi.getTransYearInfo(startdate));
        }
        return result;
    }

    @GetMapping(value = "/findTranBySeq")
    public ResultMsg findTranBySeq(@RequestParam(value = "seq") int seq){
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(transactionApi.findTranBySeq(seq));
        result.setMsg("交易信息");
        return result;
    }

    @GetMapping(value = "/findPageTrans")
    public ResultMsg findPageTrans(@RequestParam(value = "page_index") int page_index,
                                   @RequestParam(value = "page_size") int page_size){
        ResultMsg result = new ResultMsg();
        result.setCode(200);
        result.setSuccess(true);
        result.setData(transactionApi.findPageTrans(page_index,page_size));
        result.setMsg("交易信息");
        return result;
    }



    @PostMapping(value = "/send")
    public ResponseEntity sendSignRawTransaction(@RequestBody String hexData){
        ChainSDK chainSDK = new ChainSDK(chainConfig.getCryptoType());
        SignTransaction decodeTx = (SignTransaction) chainSDK.decodeTx(hexData);

        Transaction tx = null;
        try {
            final String from = decodeTx.getFrom();
            final byte[] to = decodeTx.getTo().length == 0 ? null : decodeTx.getTo();
            final byte[] data1 = decodeTx.getData();
            tx = new Transaction(from.getBytes(), to, BigInteger.ZERO, data1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Map rtnMap = Maps.newHashMap();
            rtnMap.put("status", 0);
            rtnMap.put("msg", "sig error");
            return new ResponseEntity(rtnMap, HttpStatus.OK);
        }

        //tx.setCreateContract(decodeTx.getCreateContract()>0);
        //tx.setTimestamp(decodeTx.getTimestamp());
        tx.setTran_hash(HashUtil.sha256(Hex.toHexString(tx.toString().getBytes())));
        this.txPool.put(TxPool.TXPOOL_LABEL_TRANSACTION, tx.getTran_hash(), tx);

        Map rtnMap = Maps.newHashMap();
        rtnMap.put("tx_hash", tx.getTran_hash());
        rtnMap.put("status", 1);
        return new ResponseEntity(rtnMap, HttpStatus.OK);
    }

    private String packTxMessage(Transaction tx) {
        final StringBuilder sb = new StringBuilder("Transaction{");
        sb.append("data=").append(Arrays.toString(tx.getData()));
        sb.append(", from=").append(Arrays.toString(tx.getFrom()));
        sb.append(", to=").append(Arrays.toString(tx.getTo()));
        sb.append(", value=").append(tx.getValue());
        sb.append('}');
        return sb.toString();
    }

    @GetMapping(value = "/find_by_blockhash")
    public List<Transaction> findTxByBlockHash(@RequestParam(value = "block_hash") String block_hash){
        return blockchainApi.findTxByBlockHash(block_hash);
    }

    @GetMapping(value = "/find_by_hash")
    public Transaction findByTxHash(@RequestParam(value = "hash") String hash){
        return blockchainApi.findTxByTxHash(hash);
    }

    // 接受同步区块
    @PostMapping(value = "/syncblocks")
    public String sendSyncBlocks(@RequestBody Map<String, String> data)
            throws JsonMappingException, JsonProcessingException {
        String syncBlockStr = data.get("blocklist") ;

        BlockList blocklist = JsonUtil.objectMapper.readValue(syncBlockStr, BlockList.class);

        log.info("syncBlocks(): " + blockchainServiceImpl.getMsgIp() + ":" + blockchainServiceImpl.getMsgPort() + " begin to sync blocks");

        if(blocklist == null){
            return("Get empty blocks list");
        }
        try{
            log.info("The local node starts to store the synchronized block");

            for(Block block : blocklist.getBlocklist()){
                blockchainServiceImpl.transactionExec(null,block);
                blockchainServiceImpl.storeBlock(block);
            }

            log.info("Sync blocks end. The cluster start a new round");
            blockchainServiceImpl.setSetup(true) ;
            Message message = new Message(CORE_MESSAGE_TOPIC_SYNC_END,blockchainServiceImpl.getMsgIp() + ":" + blockchainServiceImpl.getMsgPort(),blockMapper.findMaxHeight(),null);
            // 广播
            blockchainServiceImpl.broadcasting(message);
        }catch (Exception e){
            // TODO syncBlocks的异常处理
        }

        return "Syncblocks Success";
    }
}
