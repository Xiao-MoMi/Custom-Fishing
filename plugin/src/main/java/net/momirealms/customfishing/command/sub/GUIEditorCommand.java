package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.gui.page.file.FileSelector;

import java.io.File;

public class GUIEditorCommand {

    public static GUIEditorCommand INSTANCE = new GUIEditorCommand();

    public CommandAPICommand getEditorCommand() {
        return new CommandAPICommand("browser")
                .executesPlayer((player, arg) -> {
                    new FileSelector(player, new File(CustomFishingPlugin.get().getDataFolder(), "contents"));
                });
    }
}
