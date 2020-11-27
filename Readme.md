
#北航链 V1.1
## 0.简介
基于springboot框架实现的区块链基础工程，支持p2p网络通信、MPT状态树、智能合约等区块链服务，经过对V1.0版本进行重构，将模块化程度提高，共识协议可更换配置。  ——hitty
## 1.源码目录
```
com.buaa.blockchain
    annation:                           自定义注解，暂时没有其他功能 
    consensus:                          共识协议接口，区块链服务的实现类需要实现至少一个共识协议。
                                        具体的解耦方式中，core中的BlockChainServiceImpl会初始化对应的智能合约
        BaseConsensus.java              共识协议的基类，定义了一次共识的开始和消息回调
        PBFTConsensus.java:             PBFT协议的北航链版本，但是由于和MySQL交互所以无法回滚，暂时无法使用。如果仅仅使用statedb或许可以
        SBFTConsensus.java:             简化PBFT协议，删除了pre-pre阶段，详情见文档
        PBFTConsensusImpl.java          PBFT协议的具体实现类
        SBFTConsensusImpl.java          简化PBFT协议的具体实现类
    contract:                           智能合约模块
        core:
            Contract.java               智能合约代码的接口，自定义的智能合约都需要完成这个接口
            ContractManager.java        智能合约管理器
            DataUnit.java               智能合约中的数据单元
            IContractManager.java       智能合约管理器接口
            OriginContract.java         原生智能合约
        trie:                           MPT实现【勿动】
        util:                           工具类 
        State.java                      StateDB接口
        WorldState.java                 世界状态，State的实现类
    core:
        db:                             数据库抽象（未使用）
        BlockchainService.java:         区块链服务接口，定义了至少区块链至少应该实现的服务。由于交易池/持久化/逻辑处理等异构，不将其写成抽象类的形式。
        BlockchainServiceImpl.java:     对BlockchainService的实现类，并且实现了SBFTConsensus的接口。是整个程序的主要部分。   
        TimeoutHelper.java:             超时提醒（未使用）
        TxExecuter.java:                交易执行器，对接智能合约模块
        VoteHandler.java:               投票管理器，用于各种流程的投票记录
    crypto:                             区块链加密、摘要算法的相关依赖。【建议勿动】
    entity:                             实体类，封装了包括block、transaction等实体对象，包括对MYSQL的交互。
        mapper:                         mybatis的mapper，用于和MYSQl交互
        dao:                            SQL辅助
        Block.java                      区块类
        ContractAccount.java            合约账户
        ContractCaller.java             合约调用信息
        ContractEntrance.java           合约查询入口
        Times.java                      计时类
        Transaction.java                交易类
        UserAccount.java                用户类
    exception:                          自定义异常（未使用）
    message:                            区块链的p2p网络通信模块。实现类需要完成接口。
        MessageService.java:            网络通信模块接口。定义了广播、单点、集群管理等方法接口。
        MessageCallBack.java:           接收回调接口，用于将收到的数据交付给core模块
        JGroupsMessageImpl.java:        JGroups库实现的网络通信模块。
        nettyimlp:                      基于netty实现的网络通信模块。netty库自身只用于socket连接和数据传输工作
                                        需要使用netty建立Server和Client，Server用于被连接，Client用于主动连接Server
                                        本系统中A，B两个节点互联，实质上是A的Client与B的Server建立连接，并且B的Client和A的Server建立连接。
            NettyMessageImpl.java:      对MessageService接口的实现
            NettyServer.java:           建立Server端，Server端本身对连接不关注，负责接收数据并且解码
            NettyClient.java:           建立Client端，负责控制所有的channel，发起数据的编码和发送
            Netty---Handler.java:       数据收发的编解码，具体原理见netty文档
            JsonMessageProto.java:      使用Protobuf对数据进行打包，用于在Handler中的编解码
        Message:                        向上对core模块调用的Message数据结构。core模块使用Message封装需要广播的数据              
    redis:                              redis在springboot框架下的使用配置，辅助实现了基于redis的功能，如交易池等。
    test:                               不需要springboot的测试代码。
    txpool:                             交易池模块。
        Txpool:                         交易池的接口。实现类需要完成该接口。
        RedisTxPool:                    基于Redis的交易池接口。
    web:                                web接口
    utils:                              辅助工具。
    BlockchainApplication.java:         springboot规定的入口。
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
```
如下为application.properties中自定义的参数
#############buaa.blockchain##############
# 是否为调试模式
buaa.blockchain.debug=true 
# 做块门限，主节点通过主动轮询交易池来决定是否做块，当交易池的交易存量大于txgate则触发做块     
buaa.blockchain.txgate=2000
# 网络通信模块中的本地地址
buaa.blockchain.msg.ipv4=192.168.0.104
# 网络通信模块中的本地地址
buaa.blockchain.msg.port=7600
# 网络通信模块中的集群地址列表。节点会轮询尝试连接如下地址并且维护channel
buaa.blockchain.msg.address=192.168.0.104:7600,192.168.0.104:7700,192.168.0.104:7800,192.168.0.104:7900
# 版本号
buaa.blockchain.version=1.0
# 节点名
buaa.blockchain.nodename=node7600
# 节点签名
buaa.blockchain.sign=node7600_sign
# 轮询交易池的间隔时间
buaa.blockchain.round-sleeptime=2000
# 区块中交易最大数量
buaa.blockchain.tx-max-amount=1000
# 是否开启提前做块
buaa.blockchain.cache-blocks=true
# 做块中的数字摘要算法
buaa.blockchain.hash-algorithm=SHA-256
# 网络通信模块实现类，当前可选NETTY/JGROUPS
buaa.blockchain.network=NETTY
# 共识协议实现类，当前可选PBFT/SBFT
buaa.blockchain.consensus=SBFT

```

/config/jgroups-tcp.xml是网络通信模块JGrouposImpl的配置文件，实现了基于tcp的协议栈。多节点运行时需要在其中指名其他节点的地址。
如在application.properties的buaa.blockchain.network中选定JGROUPS则需要此配置文件
```
// 如下为jgroups-tcp.xml例子（部分），定义多个节点
<TCPPING timeout="3000"
             initial_hosts="${jgroups.tcpping.initial_hosts:192.168.0.102[7800],192.168.0.102[7700],192.168.0.102[7701]}"
             port_range="6"
             num_initial_members="10"/>
```
基于JGroups的通信发现了较多bug，暂不使用

源代码推荐使用IDEA作为ide来调试运行。当前实现需要同时开启Redis和MYSQL。详细配置见config

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
BlockchainService向共识模块暴露自己的引用，共识模块可以从BlockchainService中的获取消息，从而决定何时验证区块、何时存储区块。
总之，当需要新增共识协议时，完成如下的步骤：

0. 归纳共识协议的步骤，完成接口

1. 按照接口完成实现类

2. 在配置文件中注明共识模块使用的协议
