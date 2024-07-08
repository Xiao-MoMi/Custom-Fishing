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

package net.momirealms.customfishing.bukkit.command.feature;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.bukkit.util.ItemStackUtils;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.StringParser;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
public class ImportItemCommand extends BukkitCommandFeature<CommandSender> {

    public ImportItemCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("id", StringParser.stringParser())
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    Player player = context.sender();
                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                    String id = context.get("id");
                    if (itemStack.getType() == Material.AIR) {
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_IMPORT_FAILURE_NO_ITEM);
                        return;
                    }
                    File saved = new File(BukkitCustomFishingPlugin.getInstance().getDataFolder(), "imported_items.yml");
                    if (!saved.exists()) {
                        try {
                            saved.createNewFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    YamlDocument document = BukkitCustomFishingPlugin.getInstance().getConfigManager().loadData(saved);
                    Map<String, Object> map = ItemStackUtils.itemStackToMap(itemStack);
                    document.set(id, map);
                    try {
                        document.save(saved);
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_IMPORT_SUCCESS, Component.text(id));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "import_item";
    }
}
