package com.buaa.blockchain.web.controller;

import com.buaa.blockchain.api.BlockchainApi;
import com.buaa.blockchain.api.TransactionApi;
import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.mapper.TransactionMapper;
import com.buaa.blockchain.sdk.ChainSDK;
import com.buaa.blockchain.crypto.HashUtil;
import com.buaa.blockchain.crypto.utils.Hex;
import com.buaa.blockchain.sdk.model.SignTransaction;
import com.buaa.blockchain.txpool.TxPool;
import com.buaa.blockchain.utils.ResultMsg;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

        tx.setTimestamp(new Timestamp(new Date().getTime()));
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

}
