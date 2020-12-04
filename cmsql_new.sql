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
DROP database IF EXISTS bcdb7600;
create database bcdb7600;
use  bcdb7600;
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
  `largeData` longblob DEFAULT NULL,
  `data` varchar(20000) DEFAULT NULL,
  PRIMARY KEY (`tran_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of transaction
-- ----------------------------
DROP TABLE IF EXISTS `times`;
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


DROP TABLE IF EXISTS `contractAccount`;
CREATE TABLE `contractAccount` (
  `cKey` varchar(64) DEFAULT NULL,
  `cName` varchar(64) NOT NULL,
  `fullName` varchar(64) DEFAULT NULL,
  `classData` longblob DEFAULT NULL,
  `balance` int(11) DEFAULT NULL,
  `intro` varchar(1024) DEFAULT NULL,
  `classType` varchar(32) DEFAULT NULL,
  `params` varchar(32) DEFAULT NULL,
  `data` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`cName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `userAccount`;
CREATE TABLE `userAccount` (
  `userKey` varchar(64) DEFAULT NULL,
  `userName` varchar(64) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `intro` varchar(255) DEFAULT NULL,
  `balance` int(11) DEFAULT NULL,
  `data` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`userName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `company_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `company_code` varchar(45) DEFAULT NULL,
  `company_name` varchar(45) DEFAULT NULL,
  `social_credit_code` varchar(45) DEFAULT NULL,
  `change_balance` float DEFAULT NULL,
  `corporation_name` varchar(45) DEFAULT NULL,
  `corporation_id_num` varchar(45) DEFAULT NULL,
  `license_thumb` varchar(200) DEFAULT NULL,
  `audit_status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person_info`
--

DROP TABLE IF EXISTS `person_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `person_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_num` varchar(45) DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `sex` varchar(45) DEFAULT NULL,
  `change_balance` float DEFAULT NULL,
  `individual_balance` float DEFAULT NULL,
  `company_code` varchar(45) DEFAULT NULL,
  `work_status` varchar(45) DEFAULT NULL,
  `claim_status` varchar(45) DEFAULT NULL,
  `audit_status` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

