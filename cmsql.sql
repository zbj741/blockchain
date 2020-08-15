/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50624
Source Host           : localhost:3306
Source Database       : lottery

Target Server Type    : MYSQL
Target Server Version : 50624
File Encoding         : 65001

Date: 2016-01-21 20:55:55
*/
DROP database IF EXISTS blockchain; 
create database blockchain;
use  blockchain;
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `block`
-- ----------------------------
DROP TABLE IF EXISTS `block`;
 
  CREATE TABLE `block` (
  `pre_hash` varchar(100) DEFAULT NULL,
  `hash` varchar(100) NOT NULL,
  `merkle_root` tinytext,
  `state_root` tinytext,
  `pre_state_root` tinytext,
  `height` int(32) DEFAULT NULL,
  `sign` varchar(500) DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT NULL,
  `storetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tx_length` int(32) DEFAULT NULL,
  `version` varchar(32) DEFAULT NULL,
  `extra` varchar(32) DEFAULT NULL,
  `vote_result` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
  
-- ----------------------------
-- Records of block
-- ----------------------------

-- ----------------------------
-- Table structure for `transaction`
-- ----------------------------
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
  `tran_hash` varchar(100) NOT NULL,
  `block_hash` varchar(100) NOT NULL,
  `type` varchar(32) DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT NULL,
  `sequence` int(32) DEFAULT NULL,
  `tranSeq` int(32) DEFAULT NULL,
  `sign` tinytext,
  `version` varchar(32) DEFAULT NULL,
  `extra` varchar(32) DEFAULT NULL,
  `data` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`tran_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of transaction
-- ----------------------------

CREATE TABLE `times` (
  `block_hash` varchar(100) NOT NULL,
  `tx_length` int(32) DEFAULT NULL,
  `startCompute` varchar(100) DEFAULT NULL,
  `broadcast` varchar(100) DEFAULT NULL,
  `blockReceived` varchar(100) DEFAULT NULL,
  `sendVote` varchar(100) DEFAULT NULL,
  `voteReceived` varchar(100) DEFAULT NULL,
  `storeBlock` varchar(100) DEFAULT NULL,
  `removeTrans` varchar(100) DEFAULT NULL,
  `storeTrans` varchar(100) DEFAULT NULL,
  `endTime` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


  
CREATE TABLE `purchase` (
  `block_hash` varchar(100) NOT NULL,
  `projectId` varchar(100) DEFAULT NULL,
  `result` varchar(500) DEFAULT NULL,
  `timeStamp` timestamp NULL DEFAULT NULL,
  `contractId` varchar(100) DEFAULT NULL,
  `investor_uid` varchar(100) DEFAULT NULL,
  `capital` varchar(100) DEFAULT NULL,
  `interest` varchar(100) DEFAULT NULL,
  `status` varchar(100) DEFAULT NULL,
  `total` varchar(100) DEFAULT NULL,
  `deadline` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
  
  
CREATE TABLE `person` (
  `id` varchar(40) NOT NULL,
  `name` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `passwd` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `zy_coin` int(11) DEFAULT NULL,
  `ecPrivateKey` blob,
  `ecPublicKey` blob,
  `wallAddress` varchar(255) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `company` (
  `id` varchar(40) NOT NULL,
  `name` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `passwd` varchar(45) CHARACTER SET latin1 DEFAULT NULL,
  `zy_coin` int(11) DEFAULT NULL,
  `ecPrivateKey` blob,
  `ecPublicKey` blob,
  `wallAddress` varchar(255) DEFAULT NULL,
  `receipt` int(11) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `record` (
  `id` varchar(40) NOT NULL,
  `date` date DEFAULT NULL,
  `sender` varchar(255) DEFAULT NULL,
  `receiver` varchar(255) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

  
  