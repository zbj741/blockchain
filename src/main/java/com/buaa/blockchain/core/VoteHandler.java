package com.buaa.blockchain.core;

import com.buaa.blockchain.entity.Block;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录区块投票信息的辅助类
 * 本地区块链服务需要维护一个投票记录，每当收到一次投票后，查看是否触发
 * 用于表明区块投票信息的键需要由tag、height、round和blockhash一起组成，暂时定为 {blockhash}_{height}_{round}
 * 被删除的票不能被投，这里的删除可能是因为该区块已经被store或丢弃
 *
 * 清除过期key的策略，可以按照vote的执行次数或超时
 * TODO 考虑是否在本模块中管理投票是否过期？
 *
 * @author hitty
 * */
@Component
@Slf4j
public class VoteHandler {
    // 投票信息
    private ConcurrentHashMap<String,VoteRecord> voteResList= new ConcurrentHashMap<>();
    // 投票超时时限
    private Long defaultTimeout = 10000L;
    // 已被删除的key
    private ArrayList<String> removeKeys = new ArrayList<>();
    // 清空删除的key的时间间隔


    public VoteHandler() {

    }

    /**
     * 生成VoteRecord的key
     * */
    public static String createKey(String tag,int height,int round,String blockHash){
        return tag+"_"+blockHash+"_"+height+"_"+round;
    }

    /**
     * 对一轮做块投票
     * 当被投票的记录不存在时，检查已被删除的投票记录中最大高度和轮数，若该记录没有存在于之前的投票，则新建记录
     * @param tag       投票tag
     * @param height    投票区块高度
     * @param round     投票区块轮数
     * @param blockHash 投票区块hash
     * @param nodeName  投票者名字
     * @param voteValue 投票意见
     * */
    public synchronized Boolean vote(String tag,int height,int round,String blockHash,String nodeName,Boolean voteValue){
        String key = createKey(tag,height,round,blockHash);
        // 是否已经是被删除过的key
        if(removeKeys.contains(key)){
            // log.warn("vote(): removed item tag="+tag+", height="+height+", round="+round+", blockhash="+blockHash+"!");
            return false;
        }
        // 是否存在对应的投票记录
        if(!voteResList.keySet().contains(key)){
            // 生成一条记录
            VoteRecord voteRecord = new VoteRecord(tag,blockHash,height,round);
            this.voteResList.put(key,voteRecord);
        }
        voteResList.get(key).vote(voteValue,nodeName);
        return true;
    }

    /**
     * 获取当前record的赞成数量
     * */
    public int getVoteRecordAgree(String tag,int height,int round,String blockHash){
        synchronized (this){
            String key = createKey(tag,height,round,blockHash);
            if(!voteResList.keySet().contains(key)){
                return -1;
            }else{
                return voteResList.get(key).getAgree();
            }
        }
    }
    /**
     * 获取当前record的反对数量
     * */
    public int getVoteRecordAgainst(String tag,int height,int round,String blockHash){
        synchronized (this){
            String key = createKey(tag,height,round,blockHash);
            if(!voteResList.keySet().contains(key)){
                return -1;
            }else{
                return voteResList.get(key).getAgainst();
            }
        }
    }

    /**
     * 删除一条记录
     * */
    public void remove(String tag,int height,int round,String blockHash){
        synchronized (this){
            String key = createKey(tag,height,round,blockHash);
            if(voteResList.keySet().contains(key)){
                // 删除
                voteResList.remove(key);
                // 加入删除列表
                removeKeys.add(key);
            }
        }
    }


}
/**
 * 单个记录
 * */
@Data
class VoteRecord{
    private String blockHash;
    private int height;
    private int round;
    // 记录投票节点名，防止一个节点对同一个区块多次投票
    private HashSet<String> voters = new HashSet<>();
    // 使用blockHash、height、round三个参数生成一个唯一标识
    private String key;
    // 该条记录的初始化时间
    private Long startTime;
    // 超时
    Long timeout = 0L;
    // 赞成票数
    private volatile int agree = 0;
    // 反对票数
    private volatile int against = 0;

    VoteRecord(String tag,String blockHash,int height,int round){
        this.blockHash = blockHash;
        this.height = height;
        this.round = round;
        this.key = VoteHandler.createKey(tag,height,round,blockHash);
        this.startTime = System.currentTimeMillis();
    }


    /**
     * 接收来自节点的投票
     * */
    public boolean vote(boolean voteValue, String nodeName){
        // 检查超时

        // 是否在已投票节点列表中
        if(this.voters.contains(nodeName)){
            return false;
        }
        // 投票
        if(voteValue){
            this.agree++;
        }else{
            this.against++;
        }
        this.voters.add(nodeName);
        return true;
    }
}
