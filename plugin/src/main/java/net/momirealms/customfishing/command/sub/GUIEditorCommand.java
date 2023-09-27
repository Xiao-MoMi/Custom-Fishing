package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;

public class GUIEditorCommand {

    public static GUIEditorCommand INSTANCE = new GUIEditorCommand();

    public CommandAPICommand getEditorCommand() {
        return new CommandAPICommand("edit")
                .withSubcommands(

                );
    }

    private CommandAPICommand getLootCommand() {
        return new CommandAPICommand("loot")
                .withSubcommands(

                );
    }
}
