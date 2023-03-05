/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.data.storage;

public class SqlConstants {

    public static final String SQL_CREATE_BAG_TABLE = "CREATE TABLE IF NOT EXISTS `%s` ( `uuid` VARCHAR(36) NOT NULL, `version` INT NOT NULL, `size` INT NOT NULL, `contents` LONGTEXT NOT NULL, PRIMARY KEY (`uuid`) )";
    public static final String SQL_CREATE_SELL_TABLE = "CREATE TABLE IF NOT EXISTS `%s` ( `uuid` VARCHAR(36) NOT NULL, `version` INT NOT NULL, `date` INT NOT NULL, `money` INT NOT NULL, PRIMARY KEY (`uuid`) )";
    public static final String SQL_INSERT_BAG = "INSERT INTO `%s`(`uuid`, `version`, `size`, `contents`) VALUES (?, ?, ?, ?)";
    public static final String SQL_INSERT_SELL = "INSERT INTO `%s`(`uuid`, `version`, `date`, `money`) VALUES (?, ?, ?, ?)";
    public static final String SQL_UPDATE_BAG_BY_UUID = "UPDATE `%s` SET `version` = ?, `size` = ?, `contents` = ? WHERE `uuid` = ?";
    public static final String SQL_UPDATE_SELL_BY_UUID = "UPDATE `%s` SET `version` = ?, `date` = ?, `money` = ? WHERE `uuid` = ?";
    public static final String SQL_SELECT_BAG_BY_UUID = "SELECT * FROM `%s` WHERE `uuid` = ?";
    public static final String SQL_SELECT_SELL_BY_UUID = "SELECT * FROM `%s` WHERE `uuid` = ?";
    public static final String SQL_LOCK_BY_UUID = "UPDATE `%s` SET `version` = 1 WHERE `uuid` = ?";
    public static final String SQL_ALTER_TABLE = "ALTER TABLE `%s` ADD COLUMN `version` INT NOT NULL AFTER `uuid`";
    public static final String SQL_DROP_TABLE = "DROP TABLE `%s`";
}
