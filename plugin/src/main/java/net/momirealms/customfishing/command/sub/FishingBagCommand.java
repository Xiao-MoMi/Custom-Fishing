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

package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.setting.CFLocale;
import net.momirealms.customfishing.storage.user.OfflineUserImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class FishingBagCommand {

    public static FishingBagCommand INSTANCE = new FishingBagCommand();

    public CommandAPICommand getBagCommand() {
         return new CommandAPICommand("fishingbag")
                    .withPermission("fishingbag.user")
                    .withSubcommands(getEditOnlineCommand(), getEditOfflineCommand())
                    .executesPlayer(((player, args) -> {
                        if (CustomFishingPlugin.get().getBagManager().isEnabled()) {
                            var inv = CustomFishingPlugin.get().getBagManager().getOnlineBagInventory(player.getUniqueId());
                            if (inv != null) {
                                player.openInventory(inv);
                            } else {
                                AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, CFLocale.MSG_Data_Not_Loaded);
                            }
                        }
                    }));
    }

    private CommandAPICommand getEditOnlineCommand() {
        return new CommandAPICommand("edit-online")
                .withPermission("fishingbag.admin")
                .withArguments(new PlayerArgument("player"))
                .executesPlayer(((player, args) -> {
                    Player player1 = (Player) args.get("player");
                    UUID uuid = player1.getUniqueId();
                    Inventory onlineInv = CustomFishingPlugin.get().getBagManager().getOnlineBagInventory(uuid);
                    if (onlineInv != null) {
                        player.openInventory(onlineInv);
                    }
            }));
    }

    private CommandAPICommand getEditOfflineCommand() {
        return new CommandAPICommand("edit-offline")
                .withPermission("fishingbag.admin")
                .withArguments(new UUIDArgument("UUID"))
                .executesPlayer(((player, args) -> {
                    UUID uuid = (UUID) args.get("UUID");
                    Player online = Bukkit.getPlayer(uuid);
                    if (online != null) {
                        Inventory onlineInv = CustomFishingPlugin.get().getBagManager().getOnlineBagInventory(uuid);
                        if (onlineInv != null) {
                            player.openInventory(onlineInv);
                            return;
                        }
                    }
                    CustomFishingPlugin.get().getStorageManager().getOfflineUser(uuid, false).thenAccept(optional -> {
                        if (optional.isEmpty()) {
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, CFLocale.MSG_Never_Played);
                            return;
                        }
                        OfflineUser offlineUser = optional.get();
                        if (offlineUser == OfflineUserImpl.LOCKED_USER) {
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, CFLocale.MSG_Unsafe_Modification);
                            return;
                        }
                        CustomFishingPlugin.get().getScheduler().runTaskSync(() -> {
                            CustomFishingPlugin.get().getBagManager().editOfflinePlayerBag(player, offlineUser);
                        }, player.getLocation());
                    });
                }));
    }
}
