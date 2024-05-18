package net.momirealms.customfishing.bukkit.action;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.audience.Audience;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.*;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.util.ClassUtils;
import net.momirealms.customfishing.common.util.ListUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitActionManager implements ActionManager<Player> {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, ActionFactory<Player>> actionFactoryMap = new HashMap<>();
    private static final String EXPANSION_FOLDER = "expansions/action";

    public BukkitActionManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.registerBuiltInActions();
    }

    @Override
    public boolean registerAction(String type, ActionFactory<Player> actionFactory) {
        if (this.actionFactoryMap.containsKey(type)) return false;
        this.actionFactoryMap.put(type, actionFactory);
        return true;
    }

    @Override
    public boolean unregisterAction(String type) {
        return this.actionFactoryMap.remove(type) != null;
    }

    @Nullable
    @Override
    public ActionFactory<Player> getActionFactory(@NotNull String type) {
        return actionFactoryMap.get(type);
    }

    @Override
    public boolean hasAction(@NotNull String type) {
        return actionFactoryMap.containsKey(type);
    }

    @Override
    public Action<Player> parseAction(Section section) {
        ActionFactory<Player> factory = getActionFactory(section.getString("type"));
        if (factory == null) {
            plugin.getPluginLogger().warn("Action type: " + section.getString("type") + " doesn't exist.");
            return EmptyAction.INSTANCE;
        }
        return factory.process(section.get("value"), section.getDouble("chance", 1d));
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Action<Player>[] parseActions(@NotNull Section section) {
        ArrayList<Action<Player>> actionList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section innerSection) {
                Action<Player> action = parseAction(innerSection);
                if (action != null)
                    actionList.add(action);
            }
        }
        return actionList.toArray(new Action[0]);
    }

    @Override
    public Action<Player> parseAction(@NotNull String type, @NotNull Object args) {
        ActionFactory<Player> factory = getActionFactory(type);
        if (factory == null) {
            plugin.getPluginLogger().warn("Action type: " + type + " doesn't exist.");
            return EmptyAction.INSTANCE;
        }
        return factory.process(args, 1);
    }

    private void registerBuiltInActions() {
        this.registerMessageAction();
    }

    private void registerMessageAction() {
        registerAction("message", (args, chance) -> {
            List<String> messages = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                List<String> replaced = plugin.getPlaceholderManager().parse(context.getHolder(), messages, context.toPlaceholderMap());
                Audience audience = plugin.getSenderFactory().getAudience(context.getHolder());
                for (String text : replaced) {
                    audience.sendMessage(AdventureHelper.getMiniMessage().deserialize(text));
                }
            };
        });
    }

    /**
     * Loads custom ActionExpansions from JAR files located in the expansion directory.
     * This method scans the expansion folder for JAR files, loads classes that extend ActionExpansion,
     * and registers them with the appropriate action type and ActionFactory.
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    private void loadExpansions() {
        File expansionFolder = new File(plugin.getDataFolder(), EXPANSION_FOLDER);
        if (!expansionFolder.exists())
            expansionFolder.mkdirs();

        List<Class<? extends ActionExpansion<Player>>> classes = new ArrayList<>();
        File[] expansionJars = expansionFolder.listFiles();
        if (expansionJars == null) return;
        for (File expansionJar : expansionJars) {
            if (expansionJar.getName().endsWith(".jar")) {
                try {
                    Class<? extends ActionExpansion<Player>> expansionClass = (Class<? extends ActionExpansion<Player>>) ClassUtils.findClass(expansionJar, ActionExpansion.class);
                    classes.add(expansionClass);
                } catch (IOException | ClassNotFoundException e) {
                    plugin.getPluginLogger().warn("Failed to load expansion: " + expansionJar.getName(), e);
                }
            }
        }
        try {
            for (Class<? extends ActionExpansion<Player>> expansionClass : classes) {
                ActionExpansion<Player> expansion = expansionClass.getDeclaredConstructor().newInstance();
                unregisterAction(expansion.getActionType());
                registerAction(expansion.getActionType(), expansion.getActionFactory());
                plugin.getPluginLogger().info("Loaded action expansion: " + expansion.getActionType() + "[" + expansion.getVersion() + "]" + " by " + expansion.getAuthor() );
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            plugin.getPluginLogger().warn("Error occurred when creating expansion instance.", e);
        }
    }
}
