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
import net.momirealms.customfishing.data.PlayerSellData;
import net.momirealms.customfishing.data.PlayerStatisticsData;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLStorageImpl implements DataStorageInterface {

    private final SqlConnection sqlConnection;
    private final CustomFishing plugin;

    public MySQLStorageImpl(CustomFishing plugin) {
        this.plugin = plugin;
        this.sqlConnection = new SqlConnection(this);
    }

    @Override
    public void initialize() {
        sqlConnection.createNewHikariConfiguration();
        createTableIfNotExist(sqlConnection.getTablePrefix() + "_" + "fishingbag", SqlConstants.SQL_CREATE_BAG_TABLE);
        createTableIfNotExist(sqlConnection.getTablePrefix() + "_" + "selldata", SqlConstants.SQL_CREATE_SELL_TABLE);
        createTableIfNotExist(sqlConnection.getTablePrefix() + "_" + "statistics", SqlConstants.SQL_CREATE_STATS_TABLE);
    }

    @Override
    public void disable() {
        sqlConnection.close();
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.SQL;
    }

    @Override
    public Inventory loadBagData(UUID uuid, boolean force) {
        Inventory inventory = null;
        String sql = String.format(SqlConstants.SQL_SELECT_BY_UUID, sqlConnection.getTablePrefix() + "_" + "fishingbag");
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int version = rs.getInt(2);
                if (!force && version != 0) {
                    statement.close();
                    connection.close();
                    return null;
                }
                int size = rs.getInt(3);
                String contents = rs.getString(4);
                ItemStack[] itemStacks = InventoryUtils.getInventoryItems(contents);
                if (plugin.getVersionHelper().isSpigot()) inventory = Bukkit.createInventory(null, size, AdventureUtils.replaceMiniMessage(ConfigManager.fishingBagTitle.replace("{player}", Optional.ofNullable(offlinePlayer.getName()).orElse(""))));
                else inventory = Bukkit.createInventory(null, size, "{CustomFishing_Bag_" + offlinePlayer.getName() + "}");
                if (itemStacks != null) inventory.setContents(itemStacks);
                lockData(uuid, "fishingbag");
            }
            else {
                if (plugin.getVersionHelper().isSpigot()) inventory = Bukkit.createInventory(null, 9, AdventureUtils.replaceMiniMessage(ConfigManager.fishingBagTitle.replace("{player}", Optional.ofNullable(offlinePlayer.getName()).orElse(""))));
                else inventory = Bukkit.createInventory(null, 9, "{CustomFishing_Bag_" + offlinePlayer.getName() + "}");
                insertBagData(uuid, InventoryUtils.toBase64(inventory.getContents()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventory;
    }

    @Override
    public void saveBagData(UUID uuid, Inventory inventory, boolean unlock) {
        updateBagData(uuid, inventory.getSize(), InventoryUtils.toBase64(inventory.getContents()), unlock);
    }

    @Override
    public void saveBagData(Set<Map.Entry<UUID, Inventory>> set, boolean unlock) {
        String sql = String.format(SqlConstants.SQL_UPDATE_BAG_BY_UUID, sqlConnection.getTablePrefix() + "_" + "fishingbag");
        try (Connection connection = sqlConnection.getConnectionAndCheck()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Map.Entry<UUID, Inventory> entry : set) {
                    statement.setInt(1, unlock ? 0 : 1);
                    statement.setInt(2, entry.getValue().getSize());
                    statement.setString(3, InventoryUtils.toBase64(entry.getValue().getContents()));
                    statement.setString(4, entry.getKey().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            }
            catch (SQLException ex) {
                connection.rollback();
                AdventureUtils.consoleMessage("[CustomFishing] Failed to update bag data for online players");
            }
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to get connection");
        }
    }

    @Override
    public PlayerSellData loadSellData(UUID uuid, boolean force) {
        PlayerSellData playerSellData = null;
        String sql = String.format(SqlConstants.SQL_SELECT_BY_UUID, sqlConnection.getTablePrefix() + "_" + "selldata");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int version = rs.getInt(2);
                if (!force && version != 0) {
                    statement.close();
                    connection.close();
                    return null;
                }
                int date = rs.getInt(3);
                int money = rs.getInt(4);
                playerSellData = new PlayerSellData(money, date);
                lockData(uuid, "selldata");
            }
            else {
                Calendar calendar = Calendar.getInstance();
                playerSellData = new PlayerSellData(0, (calendar.get(Calendar.MONTH) +1)* 100 + calendar.get(Calendar.DATE));
                insertSellData(uuid, playerSellData.getDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerSellData;
    }

    @Override
    public void saveSellData(UUID uuid, PlayerSellData playerSellData, boolean unlock) {
        updateSellData(uuid, playerSellData.getDate(), (int) playerSellData.getMoney(), unlock);
    }

    @Override
    public void saveSellData(Set<Map.Entry<UUID, PlayerSellData>> set, boolean unlock) {
        String sql = String.format(SqlConstants.SQL_UPDATE_SELL_BY_UUID, sqlConnection.getTablePrefix() + "_" + "selldata");
        try (Connection connection = sqlConnection.getConnectionAndCheck()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Map.Entry<UUID, PlayerSellData> entry : set) {
                    statement.setInt(1, unlock ? 0 : 1);
                    statement.setInt(2, entry.getValue().getDate());
                    statement.setInt(3, (int) entry.getValue().getMoney());
                    statement.setString(4, entry.getKey().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            }
            catch (SQLException ex) {
                connection.rollback();
                AdventureUtils.consoleMessage("[CustomFishing] Failed to update sell data for all the players");
            }
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to get connection");
        }
    }

    @Override
    public PlayerStatisticsData loadStatistics(UUID uuid, boolean force) {
        PlayerStatisticsData playerStatisticsData = null;
        String sql = String.format(SqlConstants.SQL_SELECT_BY_UUID, sqlConnection.getTablePrefix() + "_" + "statistics");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int version = rs.getInt(2);
                if (!force && version != 0) {
                    statement.close();
                    connection.close();
                    return null;
                }
                String longText = rs.getString(3);
                playerStatisticsData = new PlayerStatisticsData(longText);
                lockData(uuid, "statistics");
            }
            else {
                playerStatisticsData = new PlayerStatisticsData();
                insertStatisticsData(uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerStatisticsData;
    }

    @Override
    public void saveStatistics(UUID uuid, PlayerStatisticsData statisticsData, boolean unlock) {
        updateStatisticsData(uuid, statisticsData.getLongText(), unlock);
    }

    @Override
    public void saveStatistics(Set<Map.Entry<UUID, PlayerStatisticsData>> set, boolean unlock) {
        String sql = String.format(SqlConstants.SQL_UPDATE_STATS_BY_UUID, sqlConnection.getTablePrefix() + "_" + "statistics");
        try (Connection connection = sqlConnection.getConnectionAndCheck()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (Map.Entry<UUID, PlayerStatisticsData> entry : set) {
                    statement.setInt(1, unlock ? 0 : 1);
                    statement.setString(2, entry.getValue().getLongText());
                    statement.setString(3, entry.getKey().toString());
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            }
            catch (SQLException ex) {
                connection.rollback();
                AdventureUtils.consoleMessage("[CustomFishing] Failed to update statistics data for online players");
            }
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to get connection");
        }
    }

    private void createTableIfNotExist(String table, String sqlStat) {
        String sql = String.format(sqlStat, table);
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to create table");
        }
    }

    private void insertBagData(UUID uuid, String contents) {
        String sql = String.format(SqlConstants.SQL_INSERT_BAG, sqlConnection.getTablePrefix() + "_" + "fishingbag");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setInt(2, 1);
            statement.setInt(3, 9);
            statement.setString(4, contents);
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to insert data for " + uuid);
        }
    }

    private void insertSellData(UUID uuid, int date) {
        String sql = String.format(SqlConstants.SQL_INSERT_SELL, sqlConnection.getTablePrefix() + "_" + "selldata");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setInt(2, 1);
            statement.setInt(3, date);
            statement.setInt(4, 0);
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to insert data for " + uuid);
        }
    }

    private void insertStatisticsData(UUID uuid) {
        String sql = String.format(SqlConstants.SQL_INSERT_STATS, sqlConnection.getTablePrefix() + "_" + "statistics");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setInt(2, 1);
            statement.setString(3, "");
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to insert data for " + uuid);
        }
    }

    private void updateBagData(UUID uuid, int size, String contents, boolean unlock) {
        String sql = String.format(SqlConstants.SQL_UPDATE_BAG_BY_UUID, sqlConnection.getTablePrefix() + "_" + "fishingbag");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, unlock ? 0 : 1);
            statement.setInt(2, size);
            statement.setString(3, contents);
            statement.setString(4, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to update data for " + uuid);
        }
    }

    private void updateSellData(UUID uuid, int date, int money, boolean unlock) {
        String sql = String.format(SqlConstants.SQL_UPDATE_SELL_BY_UUID, sqlConnection.getTablePrefix() + "_" + "selldata");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, unlock ? 0 : 1);
            statement.setInt(2, date);
            statement.setInt(3, money);
            statement.setString(4, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to update data for " + uuid);
        }
    }

    private void updateStatisticsData(UUID uuid, String longText, boolean unlock) {
        String sql = String.format(SqlConstants.SQL_UPDATE_STATS_BY_UUID, sqlConnection.getTablePrefix() + "_" + "statistics");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, unlock ? 0 : 1);
            statement.setString(2, longText);
            statement.setString(3, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("[CustomFishing] Failed to update data for " + uuid);
        }
    }

    public void migrate() {
        String sql_1 = String.format(SqlConstants.SQL_ALTER_TABLE, sqlConnection.getTablePrefix() + "_" + "fishingbag");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql_1)) {
            statement.executeUpdate();
            AdventureUtils.consoleMessage("<green>[CustomFishing] Tables updated");
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage(ex.getSQLState());
            AdventureUtils.consoleMessage("<red>[CustomFishing] Failed to migrate data");
        }
        String sql_2 = String.format(SqlConstants.SQL_DROP_TABLE, sqlConnection.getTablePrefix() + "_" + "sellcache");
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql_2)) {
            statement.executeUpdate();
            AdventureUtils.consoleMessage("<green>[CustomFishing] Outdated table deleted");
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage(ex.getSQLState());
            AdventureUtils.consoleMessage("<red>[CustomFishing] Failed to migrate data");
        }
    }

    public void lockData(UUID uuid, String table_suffix) {
        String sql = String.format(SqlConstants.SQL_LOCK_BY_UUID, sqlConnection.getTablePrefix() + "_" + table_suffix);
        try (Connection connection = sqlConnection.getConnectionAndCheck(); PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            AdventureUtils.consoleMessage("<red>[CustomFishing] Failed to lock data for " + uuid);
        }
    }
}
