/*
Navicat MySQL Data Transfer

Source Server         : ServerConnection
Source Server Version : 50528
Source Host           : localhost:3306
Source Database       : l2jreuniongs

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2013-04-29 16:17:06
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `achievements`
-- ----------------------------
DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
  `owner_id` int(11) NOT NULL DEFAULT '0',
  `a1` int(11) DEFAULT '0',
  `a2` int(11) DEFAULT '0',
  `a3` int(11) DEFAULT '0',
  `a4` int(11) DEFAULT '0',
  `a5` int(11) DEFAULT '0',
  `a6` int(11) DEFAULT '0',
  `a7` int(11) DEFAULT '0',
  `a8` int(11) DEFAULT '0',
  `a9` int(11) DEFAULT '0',
  `a10` int(11) DEFAULT '0',
  `a11` int(11) DEFAULT '0',
  `a12` int(11) DEFAULT '0',
  `a13` int(11) DEFAULT '0',
  `a14` int(11) DEFAULT '0',
  `a15` int(11) DEFAULT '0',
  `a16` int(11) DEFAULT '0',
  `a17` int(11) DEFAULT '0',
  `a18` int(11) DEFAULT '0',
  `a19` int(11) DEFAULT '0',
  `a20` int(11) DEFAULT '0',
  `a21` int(11) DEFAULT '0',
  `a22` int(11) DEFAULT '0',
  `a23` int(11) DEFAULT '0',
  `a24` int(11) DEFAULT '0',
  PRIMARY KEY (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of achievements
-- ----------------------------
