DROP TABLE IF EXISTS `votereward_records`;
CREATE TABLE `votereward_records` (
  `site` VARCHAR(45) NOT NULL,
  `identifier` VARCHAR(45) NOT NULL,
  `votes` SMALLINT NOT NULL DEFAULT 0,
  `lastvotedate` INT NOT NULL DEFAULT -1,
  PRIMARY KEY (`site`,`identifier`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
REPLACE INTO installed_updates (`file_name`) VALUES ("2019_02_14 02-34");