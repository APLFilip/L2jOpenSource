DROP TABLE IF EXISTS `characters`;
CREATE TABLE `characters` (
  `account_name` varchar(45) NOT NULL DEFAULT '',
  `obj_Id` int(11) NOT NULL DEFAULT '0',
  `char_name` varchar(35) NOT NULL DEFAULT '',
  `face` tinyint(3) unsigned DEFAULT NULL,
  `hairStyle` tinyint(3) unsigned DEFAULT NULL,
  `hairColor` tinyint(3) unsigned DEFAULT NULL,
  `sex` tinyint(1) DEFAULT NULL,
  `heading` mediumint(9) DEFAULT NULL,
  `x` mediumint(9) DEFAULT NULL,
  `y` mediumint(9) DEFAULT NULL,
  `z` mediumint(9) DEFAULT NULL,
  `karma` int(11) DEFAULT NULL,
  `pvpkills` int(11) DEFAULT NULL,
  `pkkills` int(11) DEFAULT NULL,
  `clanid` int(11) DEFAULT NULL,
  `createtime` int(10) unsigned NOT NULL DEFAULT '0',
  `deletetime` int(10) unsigned NOT NULL DEFAULT '0',
  `title` varchar(16) DEFAULT NULL,
  `rec_have` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `rec_left` tinyint(3) unsigned NOT NULL DEFAULT '20',
  `rec_bonus_time` int(11) NOT NULL DEFAULT '3600',
  `hunt_points` int(11) NOT NULL DEFAULT '0',
  `hunt_time` int(11) NOT NULL DEFAULT '14400',
  `accesslevel` tinyint(4) DEFAULT NULL,
  `online` tinyint(1) DEFAULT NULL,
  `onlinetime` int(10) unsigned NOT NULL DEFAULT '0',
  `lastAccess` int(10) unsigned NOT NULL DEFAULT '0',
  `leaveclan` int(10) unsigned NOT NULL DEFAULT '0',
  `deleteclan` int(10) unsigned NOT NULL DEFAULT '0',
  `nochannel` int(11) NOT NULL DEFAULT '0',
  `pledge_type` smallint(6) NOT NULL DEFAULT '-128',
  `pledge_rank` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `lvl_joined_academy` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `apprentice` int(10) unsigned NOT NULL DEFAULT '0',
  `key_bindings` varbinary(8192) DEFAULT NULL,
  `pcBangPoints` int(11) NOT NULL DEFAULT '0',
  `vitality` smallint(5) unsigned NOT NULL DEFAULT '20000',
  `fame` int(11) NOT NULL DEFAULT '0',
  `bookmarks` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `bot_report_points` int(10) NOT NULL DEFAULT '7',
  `bot_rating` int(11) NOT NULL DEFAULT '0',
  `hwid_lock` varchar(40) DEFAULT '',
  `lastVoteHopzone` bigint(20) unsigned DEFAULT NULL,
  `lastVoteTopzone` bigint(20) unsigned DEFAULT NULL,
  `hasVotedHop` int(10) unsigned DEFAULT '0',
  `hasVotedTop` int(10) unsigned DEFAULT '0',
  `monthVotes` int(10) unsigned DEFAULT '0',
  `totalVotes` int(10) unsigned DEFAULT '0',
  `tries` int(10) unsigned DEFAULT '3',
  PRIMARY KEY (`obj_Id`),
  UNIQUE KEY `char_name` (`char_name`),
  KEY `account_name` (`account_name`),
  KEY `clanid` (`clanid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
