/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.command.feature;

import com.saicone.rtag.item.ItemTagStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

public class DebugSNBTCommand extends BukkitCommandFeature<CommandSender> {

    public DebugSNBTCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .handler(context -> {
                    ItemStack itemStack = context.sender().getInventory().getItemInMainHand();
                    if (itemStack == null || itemStack.getType() == Material.AIR) return;
                    String snbt = ItemTagStream.INSTANCE.toString(itemStack);
                    BukkitCustomFishingPlugin.getInstance().getSenderFactory().wrap(context.sender())
                            .sendMessage(Component.text(snbt).hoverEvent(HoverEvent.showText(Component.text("Copy").color(NamedTextColor.GREEN))).clickEvent(ClickEvent.copyToClipboard(snbt)));
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_snbt";
    }
}
