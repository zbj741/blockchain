package com.buaa.blockchain.core;

import com.buaa.blockchain.entity.Block;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录区块投票信息的辅助类
 * 本地区块链服务需要维护一个投票记录，每当收到一次投票后，查看是否触发
 * 用于表明区块投票信息的键需要由height、round和blockhash一起组成，暂时定为 {blockhash}_{height}_{round}
 *
 * TODO 考虑是否在本模块中管理投票是否过期？
 *
 * @author hitty
 * */
@Component
public class VoteHandler {
    // 投票信息
    private ConcurrentHashMap<String,VoteRecord> voteResList= new ConcurrentHashMap<>();
    // 投票超时时限
    private final Long defaultTimeout = 20000L;
    // 已被删除的key
    // TODO 长度管理
    private ArrayList<String> removeKeys = new ArrayList<>();

    public VoteHandler() {

    }

    /**
     * 生成VoteRecord的key
     * */
    public static String createKey(int height,int round,String blockHash){
        return blockHash+"_"+height+"_"+round;
    }


    /**
     * 对一轮做块投票
     * 当被投票的记录不存在时，检查已被删除的投票记录中最大高度和轮数，若该记录没有存在于之前的投票，则新建记录
     * @param height
     * @param round
     * @param blockHash
     * @param voteValue 投票意见
     * */
    public void vote(int height,int round,String blockHash,String nodeName,Boolean voteValue){
        synchronized (this){
            String key = createKey(height,round,blockHash);
            // 是否已经是被删除过的key
            if(removeKeys.contains(key)){
                return;
            }
            // 是否存在对应的投票记录
            if(!voteResList.keySet().contains(key)){
                // 生成一条记录
                VoteRecord voteRecord = new VoteRecord(blockHash,height,round);
                this.voteResList.put(key,voteRecord);
            }
            voteResList.get(key).vote(voteValue,nodeName);
        }
    }

    /**
     * 获取当前record的赞成数量
     * */
    public int getVoteRecordAgree(int height,int round,String blockHash){
        synchronized (this){
            String key = createKey(height,round,blockHash);
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
    public int getVoteRecordAgainst(int height,int round,String blockHash){
        synchronized (this){
            String key = createKey(height,round,blockHash);
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
    public void remove(int height,int round,String blockHash){
        synchronized (this){
            String key = createKey(height,round,blockHash);
            if(voteResList.keySet().contains(key)){
                // 删除
                voteResList.remove(key);
                // 加入删除列表
                removeKeys.add(key);
            }
        }
    }


    public static void main(String[] args) {
        Long ii = 4L;
        System.out.println(ii*(2/3.0));
        String bh = "blocktest";
        int h = 1;
        int r = 2;
        VoteHandler voteHandler = new VoteHandler();

        voteHandler.vote(h,r,bh,"node1",true);
        for(int i = 0;i < 5;i++){
            voteHandler.vote(h,r,bh,"node"+i,i%2==0);
            System.out.println(voteHandler.getVoteRecordAgree(h,r,bh));
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
    // Long timeout
    // 赞成票数
    private int agree = 0;
    // 反对票数
    private int against = 0;

    VoteRecord(String blockHash,int height,int round){
        this.blockHash = blockHash;
        this.height = height;
        this.round = round;
        this.key = VoteHandler.createKey(height,round,blockHash);
        this.startTime = System.currentTimeMillis();
    }


    /**
     * 接收来自节点的投票
     * */
    public boolean vote(boolean voteValue, String nodeName){
        // TODO 超时

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
