CREATE TABLE IF NOT EXISTS `prime_shop` (
  `charId` INT UNSIGNED NOT NULL DEFAULT 0,
  `productId` INT NOT NULL DEFAULT 0,
  `quantity` INT NOT NULL DEFAULT 0,
  `maxStock` INT NOT NULL DEFAULT 0,
  `transactionTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;