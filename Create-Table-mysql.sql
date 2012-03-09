CREATE TABLE `%1s` (
  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY ,
  `player` VARCHAR( 16 ) NOT NULL ,
  `permission` VARCHAR( 64 ) NOT NULL ,
  `remainingUses` INT NOT NULL DEFAULT '0',
  `expires` DOUBLE NOT NULL DEFAULT '0'
) ENGINE=InnoDB;