DROP TABLE IF EXISTS `clan_largecrests`;
CREATE TABLE `clan_largecrests` (
	`clan_id` INT NOT NULL DEFAULT '0',
	`crest_part` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`data` BLOB(14336) NULL DEFAULT NULL,
	PRIMARY KEY (`clan_id`, `crest_part`)
) ENGINE=MyISAM;
REPLACE INTO installed_updates (`file_name`) VALUES ("2019_01_05 05-25");