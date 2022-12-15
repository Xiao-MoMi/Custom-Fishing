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

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.data.PlayerBagData;
import net.momirealms.customfishing.data.PlayerSellData;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.UUID;

public class MySQLStorageImpl implements DataStorageInterface {

    public static SqlConnection sqlConnection = new SqlConnection();

    @Override
    public void initialize() {
        sqlConnection.createNewHikariConfiguration();
        createTableIfNotExist(sqlConnection.getTablePrefix() + "_fishingbag", SqlConstants.SQL_CREATE_BAG_TABLE);
        createTableIfNotExist(sqlConnection.getTablePrefix() + "_sellcache", SqlConstants.SQL_CREATE_SELL_TABLE);
    }

    @Override
    public void disable() {
        sqlConnection.close();
    }

    @Override
    public Inventory loadBagData(OfflinePlayer player) {
        Inventory inventory = null;
        try {
            Connection connection = sqlConnection.getConnectionAndCheck();
            String sql = String.format(SqlConstants.SQL_SELECT_BAG_BY_UUID, sqlConnection.getTablePrefix() + "_fishingbag");;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, player.getUniqueId().toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int size = rs.getInt(2);
                String contents = rs.getString(3);
                ItemStack[] itemStacks = InventoryUtil.getInventoryItems(contents);
                inventory = Bukkit.createInventory(null, size, "{CustomFishing_Bag_" + player.getName() + "}");
                if (itemStacks != null) inventory.setContents(itemStacks);
            }
            else {
                inventory = Bukkit.createInventory(null, 9, "{CustomFishing_Bag_" + player.getName() + "}");
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventory;
    }

    @Override
    public void saveBagData(PlayerBagData playerBagData) {
        UUID uuid = playerBagData.getPlayer().getUniqueId();
        Inventory inventory = playerBagData.getInventory();
        String contents = InventoryUtil.toBase64(inventory.getContents());
        if (contents == null) contents = "";
        if (exists(uuid, SqlConstants.SQL_SELECT_BAG_BY_UUID, "fishingbag")) {
            updateBagData(uuid, inventory.getSize(), contents);
        }
        else {
            insertBagData(uuid, inventory.getSize(), contents);
        }
    }

    @Override
    public void loadSellCache(Player player) {
        try {
            Connection connection = sqlConnection.getConnectionAndCheck();
            String sql = String.format(SqlConstants.SQL_SELECT_SELL_BY_UUID, sqlConnection.getTablePrefix() + "_sellcache");
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, player.getUniqueId().toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int date = rs.getInt(2);
                int money = rs.getInt(3);
                CustomFishing.plugin.getSellManager().loadPlayerToCache(player.getUniqueId(), date, money);
            }
            else {
                CustomFishing.plugin.getSellManager().loadPlayerToCache(player.getUniqueId(), Calendar.getInstance().get(Calendar.DATE), 0);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSellCache(UUID uuid, PlayerSellData playerSellData) {
        if (exists(uuid, SqlConstants.SQL_SELECT_SELL_BY_UUID, "sellcache")) {
            updateSellData(uuid, playerSellData.getDate(), (int) playerSellData.getMoney());
        }
        else {
            insertSellData(uuid, playerSellData.getDate(), (int) playerSellData.getMoney());
        }
    }

    private void createTableIfNotExist(String table, String sqlStat) {
        String sql = String.format(sqlStat, table);
        try {
            Connection connection = sqlConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            AdventureUtil.consoleMessage("[CustomFishing] Failed to create table");
        }
    }

    private void insertBagData(UUID uuid, int size, String contents) {
        String sql = String.format(SqlConstants.SQL_INSERT_BAG, sqlConnection.getTablePrefix() + "_fishingbag");
        try {
            Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            statement.setInt(2, size);
            statement.setString(3, contents);
            statement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            AdventureUtil.consoleMessage("[CustomFishing] Failed to insert data for " + uuid);
        }
    }

    private void insertSellData(UUID uuid, int date, int money) {
        String sql = String.format(SqlConstants.SQL_INSERT_SELL, sqlConnection.getTablePrefix() + "_sellcache");
        try {
            Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            statement.setInt(2, date);
            statement.setInt(3, money);
            statement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            AdventureUtil.consoleMessage("[CustomFishing] Failed to insert data for " + uuid);
        }
    }

    private void updateBagData(UUID uuid, int size, String contents) {
        String sql = String.format(SqlConstants.SQL_UPDATE_BAG_BY_UUID, sqlConnection.getTablePrefix() + "_fishingbag");
        try {
            Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, size);
            statement.setString(2, contents);
            statement.setString(3, uuid.toString());
            statement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            AdventureUtil.consoleMessage("[CustomFishing] Failed to update data for " + uuid);
        }
    }

    private void updateSellData(UUID uuid, int date, int money) {
        String sql = String.format(SqlConstants.SQL_UPDATE_SELL_BY_UUID, sqlConnection.getTablePrefix() + "_sellcache");
        try {
            Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, date);
            statement.setInt(2, money);
            statement.setString(3, uuid.toString());
            statement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            AdventureUtil.consoleMessage("[CustomFishing] Failed to update data for " + uuid);
        }
    }

    public boolean exists(UUID uuid, String sqlStat, String suffix) {
        String sql = String.format(sqlStat, sqlConnection.getTablePrefix() + "_" + suffix);
        boolean exist;
        try {
            Connection connection = sqlConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            exist = rs.next();
            connection.close();
        } catch (SQLException ex) {
            AdventureUtil.consoleMessage("[CustomFishing] Failed to select data for " + uuid);
            return false;
        }
        return exist;
    }
}
