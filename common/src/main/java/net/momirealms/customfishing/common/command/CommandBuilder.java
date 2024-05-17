package net.momirealms.customfishing.common.command;

import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

public interface CommandBuilder<C> {

    CommandBuilder<C> setPermission(String permission);

    CommandBuilder<C> setCommandNode(String... subNodes);

    Command.Builder<C> getBuiltCommandBuilder();

    class BasicCommandBuilder<C> implements CommandBuilder<C> {

        private Command.Builder<C> commandBuilder;

        public BasicCommandBuilder(CommandManager<C> commandManager, String rootNode) {
            this.commandBuilder = commandManager.commandBuilder(rootNode);
        }

        @Override
        public CommandBuilder<C> setPermission(String permission) {
            this.commandBuilder = this.commandBuilder.permission(permission);
            return this;
        }

        @Override
        public CommandBuilder<C> setCommandNode(String... subNodes) {
            for (String sub : subNodes) {
                this.commandBuilder = this.commandBuilder.literal(sub);
            }
            return this;
        }

        @Override
        public Command.Builder<C> getBuiltCommandBuilder() {
            return commandBuilder;
        }
    }
}
