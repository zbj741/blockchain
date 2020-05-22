
#北航链 V1.1
## 0.简介
基于springboot框架实现的区块链基础工程，支持p2p网络通信、MPT状态树、智能合约等区块链服务，经过对V1.0版本进行重构，将模块化程度提高，共识协议可更换配置。  ——hitty
## 1.源码目录
```
com.buaa.blockchain
    annation:   自定义注解
    consensus:  共识协议接口，区块链服务的实现类需要实现至少一个共识协议
        SBFTConsensus:  简单PBFT协议，删除了pre-pre阶段，详情见文档
    contract:
    core:
        BlockchainService:  区块链服务接口，定义了至少区块链至少应该实现的服务。由于交易池/持久化/逻辑处理等异构，不将其写成抽象类的形式。
        BlockchainServiceImpl:  对BlockchainService的实现类，并且实现了SBFTConsensus的接口。是整个程序的主要部分。     
    crypto:     区块链加密相关依赖。【勿动】
    dao:        持久化相关代码。
    datasource: 用于规范MPT持久化的接口。暂时的实现参考了以太坊的LevelDb实现。
    entity:     实体类，封装了包括block、transaction等实体对象。
    mapper:     辅助mybatis的mapper，用于和mysql交互。
    message:    区块链的p2p网络通信模块。实现类需要完成接口。
        MessageService:     网络通信模块接口。定义了广播、单点、集群管理等方法接口。
        MessageCallBack:    接收回调接口，辅助定义了p2p网络的功能接口。
        JGroupsMessageImpl: JGroups库实现的网络通信模块。
    redis:      redis在springboot框架下的使用配置，辅助实现了基于redis的功能，如交易池等。
    test:       不需要springboot的测试代码。
    trie:       MPT底层实现，配合datasource和utils使用，实现参考以太坊。【勿动】
    txpool:     交易池模块。
        Txpool:         交易池的接口。实现类需要完成该接口。
        RedisTxPool:    基于Redis的交易池接口。
    utils:  辅助工具。
    BlockchainApplication:  springboot规定的入口。
```

## 2.BlockchainServiceImpl的执行流程

0.初始化

1.主节点从交易池中取出交易数据，打包区块并第一次广播

2.全网共识第一次广播，计算并投票，将投票结果进行第二次广播

3.全部节点接收第二次广播，根据投票执行对应操作
    3.1.投票赞成通过，则执行区块
    3.2.投票未通过，则无操作
    
4.全部节点本地计算主节点，开始下一轮做块（回到1）

## 3.配置与测试
本工程基于maven，可以通过maven package将其打包为jar文件直接运行。

测试环境可以只保存/blockchain.jar、/config/、/log/三个文件。

/config/application.properties是springboot的配置文件，配置了区块链运行的各种参数。

/config/jgroups-tcp.xml是网络通信模块JGrouposImpl的配置文件，实现了基于tcp的协议栈。多节点运行时需要在其中指名其他节点的地址。
```
// 如下为jgroups-tcp.xml例子（部分），定义多个节点
<TCPPING timeout="3000"
             initial_hosts="${jgroups.tcpping.initial_hosts:192.168.0.102[7800],192.168.0.102[7700],192.168.0.102[7701]}"
             port_range="6"
             num_initial_members="10"/>
```
源代码推荐使用IDEA作为ide来调试运行。当前实现需要同时开启Redis和MYSQL。

## 3.共识模块与区块链模块
共识模块功能和区块链基础功能的分离，是设计的出发点之一。本项目通过将区块链模块和共识模块以接口的形式定义，并各自完成其实现类，从而解耦。
区块链服务接口如下：
```
// com.buaa.blockchain.core.BlockchainService
public interface BlockchainService {
    void startNewRound(int height,int round);
    void firstTimeSetup();
    void storeBlock(Block block);
    boolean verifyBlock(Block block, int height, int round);
    Block generateFirstBlock();
    ......
}
```
区块链服务BlockchainService的实现类需要完成这些接口的实现，但是这些接口和共识模块无关。
共识模块的接口（以SBFT为例）：
```
// com.buaa.blockchain.consensus.SBFTConsensus
public interface SBFTConsensus<T> extends BaseConsensus<T>{
    void sbftDigestBroadcast(T stage1_send);
    void sbftDigestBroadcastReceived(T stage1_received);
    void sbftVoteBroadcast(T stage2_send);
    void sbftVoteBroadcastReceived(T stage2_received);
    void sbftExecute(T exec);
}
```
使用接口的形式，将每一个共识环节标注清楚。
在需要增添新的共识协议时，不需要更改已有的区块链服务实现类，而且需要完成新的共识协议接口和其实现类。
如com.buaa.blockchain.consensus.SBFTConsensusImpl.java为SBFT协议的实现类，实现了SBFTConsensus接口。
在SBFTConsensusImpl的开发中，通过持有BlockchainService的引用，使得其拥有区块链的功能，如消息的发送和接收等。
BlockchainService向共识模块暴露自己的引用，共识模块可以重写BlockchainService中的消息收发，从而决定何时验证区块、何时存储区块。
总之，当需要新增共识协议时，完成如下的步骤：

0. 归纳共识协议的步骤，完成接口

1. 按照接口完成实现类

2. 在配置文件中注明共识模块使用的协议
