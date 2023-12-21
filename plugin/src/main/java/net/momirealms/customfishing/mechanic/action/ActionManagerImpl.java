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

package net.momirealms.customfishing.mechanic.action;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.ActionManager;
import net.momirealms.customfishing.api.manager.LootManager;
import net.momirealms.customfishing.api.mechanic.GlobalSettings;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionExpansion;
import net.momirealms.customfishing.api.mechanic.action.ActionFactory;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.VaultHook;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.setting.CFLocale;
import net.momirealms.customfishing.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ActionManagerImpl implements ActionManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, ActionFactory> actionBuilderMap;
    private final String EXPANSION_FOLDER = "expansions/action";

    public ActionManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.actionBuilderMap = new HashMap<>();
        this.registerInbuiltActions();
    }

    // Method to register various built-in actions during initialization.
    private void registerInbuiltActions() {
        this.registerMessageAction();
        this.registerCommandAction();
        this.registerMendingAction();
        this.registerExpAction();
        this.registerChainAction();
        this.registerPotionAction();
        this.registerSoundAction();
        this.registerPluginExpAction();
        this.registerTitleAction();
        this.registerActionBarAction();
        this.registerCloseInvAction();
        this.registerDelayedAction();
        this.registerConditionalAction();
        this.registerPriorityAction();
        this.registerLevelAction();
        this.registerHologramAction();
        this.registerFakeItemAction();
        this.registerFishFindAction();
        this.registerFoodAction();
        this.registerItemAmountAction();
        this.registerItemDurabilityAction();
        this.registerGiveItemAction();
        this.registerMoneyAction();
    }

    // Method to load expansions and global event actions.
    public void load() {
        this.loadExpansions();
        this.loadGlobalEventActions();
    }

    public void unload() {
        GlobalSettings.unload();
    }

    public void disable() {
        unload();
        this.actionBuilderMap.clear();
    }

    // Method to load global event actions from the plugin's configuration file.
    private void loadGlobalEventActions() {
        YamlConfiguration config = plugin.getConfig("config.yml");
        GlobalSettings.loadEvents(config.getConfigurationSection("mechanics.global-events"));
    }

    /**
     * Registers an ActionFactory for a specific action type.
     * This method allows you to associate an ActionFactory with a custom action type.
     *
     * @param type           The custom action type to register.
     * @param actionFactory  The ActionFactory responsible for creating actions of the specified type.
     * @return True if the registration was successful (the action type was not already registered), false otherwise.
     */
    @Override
    public boolean registerAction(String type, ActionFactory actionFactory) {
        if (this.actionBuilderMap.containsKey(type)) return false;
        this.actionBuilderMap.put(type, actionFactory);
        return true;
    }

    /**
     * Unregisters an ActionFactory for a specific action type.
     * This method allows you to remove the association between an action type and its ActionFactory.
     *
     * @param type The custom action type to unregister.
     * @return True if the action type was successfully unregistered, false if it was not found.
     */
    @Override
    public boolean unregisterAction(String type) {
        return this.actionBuilderMap.remove(type) != null;
    }

    /**
     * Retrieves an Action object based on the configuration provided in a ConfigurationSection.
     * This method reads the type of action from the section, obtains the corresponding ActionFactory,
     * and builds an Action object using the specified values and chance.
     *
     * @param section The ConfigurationSection containing the action configuration.
     * @return An Action object created based on the configuration, or an EmptyAction instance if the action type is invalid.
     */
    @Override
    @NotNull
    public Action getAction(ConfigurationSection section) {
        ActionFactory factory = getActionFactory(section.getString("type"));
        if (factory == null) {
            LogUtils.warn("Action type: " + section.getString("type") + " doesn't exist.");
            // to prevent NPE
            return EmptyAction.instance;
        }
        return factory.build(
                        section.get("value"),
                        section.getDouble("chance", 1d)
                );
    }

    /**
     * Retrieves a mapping of ActionTriggers to arrays of Actions from a ConfigurationSection.
     * This method iterates through the provided ConfigurationSection to extract action triggers
     * and their associated arrays of Actions.
     *
     * @param section The ConfigurationSection containing action mappings.
     * @return A HashMap where keys are ActionTriggers and values are arrays of Action objects.
     */
    @Override
    @NotNull
    public HashMap<ActionTrigger, Action[]> getActionMap(ConfigurationSection section) {
        // Create an empty HashMap to store the action mappings
        HashMap<ActionTrigger, Action[]> actionMap = new HashMap<>();

        // If the provided ConfigurationSection is null, return the empty actionMap
        if (section == null) return actionMap;

        // Iterate through all key-value pairs in the ConfigurationSection
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                // Convert the key to an ActionTrigger enum (assuming it's in uppercase English)
                // and map it to an array of Actions obtained from the inner section
                try {
                    actionMap.put(
                            ActionTrigger.valueOf(entry.getKey().toUpperCase(Locale.ENGLISH)),
                            getActions(innerSection)
                    );
                } catch (IllegalArgumentException e) {
                    LogUtils.warn("Event: " + entry.getKey() + " doesn't exist!");
                }
            }
        }
        return actionMap;
    }

    /**
     * Retrieves an array of Action objects from a ConfigurationSection.
     * This method iterates through the provided ConfigurationSection to extract Action configurations
     * and build an array of Action objects.
     *
     * @param section The ConfigurationSection containing action configurations.
     * @return An array of Action objects created based on the configurations in the section.
     */
    @NotNull
    @Override
    public Action[] getActions(ConfigurationSection section) {
        // Create an ArrayList to store the Actions
        ArrayList<Action> actionList = new ArrayList<>();
        if (section == null) return actionList.toArray(new Action[0]);

        // Iterate through all key-value pairs in the ConfigurationSection
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                actionList.add(getAction(innerSection));
            }
        }
        return actionList.toArray(new Action[0]);
    }

    /**
     * Retrieves an ActionFactory associated with a specific action type.
     *
     * @param type The action type for which to retrieve the ActionFactory.
     * @return The ActionFactory associated with the specified action type, or null if not found.
     */
    @Nullable
    @Override
    public ActionFactory getActionFactory(String type) {
        return actionBuilderMap.get(type);
    }

    /**
     * Loads custom ActionExpansions from JAR files located in the expansion directory.
     * This method scans the expansion folder for JAR files, loads classes that extend ActionExpansion,
     * and registers them with the appropriate action type and ActionFactory.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadExpansions() {
        File expansionFolder = new File(plugin.getDataFolder(), EXPANSION_FOLDER);
        if (!expansionFolder.exists())
            expansionFolder.mkdirs();

        List<Class<? extends ActionExpansion>> classes = new ArrayList<>();
        File[] expansionJars = expansionFolder.listFiles();
        if (expansionJars == null) return;
        for (File expansionJar : expansionJars) {
            if (expansionJar.getName().endsWith(".jar")) {
                try {
                    Class<? extends ActionExpansion> expansionClass = ClassUtils.findClass(expansionJar, ActionExpansion.class);
                    classes.add(expansionClass);
                } catch (IOException | ClassNotFoundException e) {
                    LogUtils.warn("Failed to load expansion: " + expansionJar.getName(), e);
                }
            }
        }
        try {
            for (Class<? extends ActionExpansion> expansionClass : classes) {
                ActionExpansion expansion = expansionClass.getDeclaredConstructor().newInstance();
                unregisterAction(expansion.getActionType());
                registerAction(expansion.getActionType(), expansion.getActionFactory());
                LogUtils.info("Loaded action expansion: " + expansion.getActionType() + "[" + expansion.getVersion() + "]" + " by " + expansion.getAuthor() );
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            LogUtils.warn("Error occurred when creating expansion instance.", e);
        }
    }

    /**
     * Retrieves a mapping of success times to corresponding arrays of actions from a ConfigurationSection.
     *
     * @param section The ConfigurationSection containing success times actions.
     * @return A HashMap where success times associated with actions.
     */
    @Override
    public HashMap<Integer, Action[]> getTimesActionMap(ConfigurationSection section) {
        HashMap<Integer, Action[]> actionMap = new HashMap<>();
        if (section == null) return actionMap;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                actionMap.put(Integer.parseInt(entry.getKey()), plugin.getActionManager().getActions(innerSection));
            }
        }
        return actionMap;
    }

    /**
     * Triggers a list of actions with the given condition.
     * If the list of actions is not null, each action in the list is triggered.
     *
     * @param actions   The list of actions to trigger.
     * @param condition The condition associated with the actions.
     */
    @Override
    public void triggerActions(List<Action> actions, Condition condition) {
        if (actions != null)
            for (Action action : actions)
                action.trigger(condition);
    }

    private void registerMessageAction() {
        registerAction("message", (args, chance) -> {
            ArrayList<String> msg = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                        condition.getPlayer(),
                        msg,
                        condition.getArgs()
                );
                for (String text : replaced) {
                    AdventureManagerImpl.getInstance().sendPlayerMessage(condition.getPlayer(), text);
                }
            };
        });
        registerAction("broadcast", (args, chance) -> {
            ArrayList<String> msg = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                        condition.getPlayer(),
                        msg,
                        condition.getArgs()
                );
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (String text : replaced) {
                        AdventureManagerImpl.getInstance().sendPlayerMessage(player, text);
                    }
                }
            };
        });
        registerAction("message-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<String> msg = section.getStringList("message");
                int range = section.getInt("range");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    plugin.getScheduler().runTaskSync(() -> {
                        for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                            if (distance <= range) {
                                condition.insertArg("{near}", player.getName());
                                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                                        owner,
                                        msg,
                                        condition.getArgs()
                                );
                                for (String text : replaced) {
                                    AdventureManagerImpl.getInstance().sendPlayerMessage((Player) player, text);
                                }
                                condition.delArg("{near}");
                            }
                        }
                    }, condition.getLocation());
                };
            } else {
                LogUtils.warn("Illegal value format found at action: message-nearby");
                return EmptyAction.instance;
            }
        });
        registerAction("random-message", (args, chance) -> {
            ArrayList<String> msg = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                String random = msg.get(ThreadLocalRandom.current().nextInt(msg.size()));
                random = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), random, condition.getArgs());
                AdventureManagerImpl.getInstance().sendPlayerMessage(condition.getPlayer(), random);
            };
        });
    }

    private void registerCommandAction() {
        registerAction("command", (args, chance) -> {
            ArrayList<String> cmd = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                        condition.getPlayer(),
                        cmd,
                        condition.getArgs()
                );
                plugin.getScheduler().runTaskSync(() -> {
                    for (String text : replaced) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
                    }
                }, condition.getLocation());
            };
        });
        registerAction("random-command", (args, chance) -> {
            ArrayList<String> cmd = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                String random = cmd.get(ThreadLocalRandom.current().nextInt(cmd.size()));
                random = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), random, condition.getArgs());
                String finalRandom = random;
                plugin.getScheduler().runTaskSync(() -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalRandom);
                }, condition.getLocation());
            };
        });
        registerAction("command-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<String> cmd = section.getStringList("command");
                int range = section.getInt("range");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    plugin.getScheduler().runTaskSync(() -> {
                        for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                            if (distance <= range) {
                                condition.insertArg("{near}", player.getName());
                                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                                        owner,
                                        cmd,
                                        condition.getArgs()
                                );
                                for (String text : replaced) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
                                }
                                condition.delArg("{near}");
                            }
                        }
                    }, condition.getLocation());
                };
            } else {
                LogUtils.warn("Illegal value format found at action: command-nearby");
                return EmptyAction.instance;
            }
        });
    }

    private void registerCloseInvAction() {
        registerAction("close-inv", (args, chance) -> condition -> {
            if (Math.random() > chance) return;
            condition.getPlayer().closeInventory();
        });
    }

    private void registerActionBarAction() {
        registerAction("actionbar", (args, chance) -> {
            String text = (String) args;
            return condition -> {
                if (Math.random() > chance) return;
                String parsed = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), text, condition.getArgs());
                AdventureManagerImpl.getInstance().sendActionbar(condition.getPlayer(), parsed);
            };
        });
        registerAction("random-actionbar", (args, chance) -> {
            ArrayList<String> texts = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                String random = texts.get(ThreadLocalRandom.current().nextInt(texts.size()));
                random = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), random, condition.getArgs());
                AdventureManagerImpl.getInstance().sendActionbar(condition.getPlayer(), random);
            };
        });
        registerAction("actionbar-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String actionbar = section.getString("actionbar");
                int range = section.getInt("range");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    plugin.getScheduler().runTaskSync(() -> {
                        for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                            if (distance <= range) {
                                condition.insertArg("{near}", player.getName());
                                String replaced = PlaceholderManagerImpl.getInstance().parse(
                                        owner,
                                        actionbar,
                                        condition.getArgs()
                                );
                                AdventureManagerImpl.getInstance().sendActionbar((Player) player, replaced);
                                condition.delArg("{near}");
                            }
                        }
                        }, condition.getLocation()
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: command-nearby");
                return EmptyAction.instance;
            }
        });
    }

    private void registerMendingAction() {
        registerAction("mending", (args, chance) -> {
            var value = ConfigUtils.getValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                if (CustomFishingPlugin.get().getVersionManager().isSpigot()) {
                    condition.getPlayer().getLocation().getWorld().spawn(condition.getPlayer().getLocation(), ExperienceOrb.class, e -> e.setExperience((int) value.get(condition.getPlayer())));
                } else {
                    condition.getPlayer().giveExp((int) value.get(condition.getPlayer()), true);
                    AdventureManagerImpl.getInstance().sendSound(condition.getPlayer(), Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
                }
            };
        });
    }

    private void registerFoodAction() {
        registerAction("food", (args, chance) -> {
            var value = ConfigUtils.getValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                Player player = condition.getPlayer();
                player.setFoodLevel((int) (player.getFoodLevel() + value.get(player)));
            };
        });
        registerAction("saturation", (args, chance) -> {
            var value = ConfigUtils.getValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                Player player = condition.getPlayer();
                player.setSaturation((float) (player.getSaturation() + value.get(player)));
            };
        });
    }

    private void registerExpAction() {
        registerAction("exp", (args, chance) -> {
            var value = ConfigUtils.getValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                condition.getPlayer().giveExp((int) value.get(condition.getPlayer()));
                AdventureManagerImpl.getInstance().sendSound(condition.getPlayer(), Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
            };
        });
    }

    private void registerHologramAction() {
        registerAction("hologram", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String text = section.getString("text", "");
                int duration = section.getInt("duration", 20);
                boolean position = section.getString("position", "other").equals("other");
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                int range = section.getInt("range", 16);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    Location location = position ? condition.getLocation() : owner.getLocation();
                    plugin.getScheduler().runTaskSync(() -> {
                            for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                                double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                                if (distance <= range) {
                                    ArmorStandUtils.sendHologram(
                                            (Player) player,
                                            location.clone().add(x, y, z),
                                            AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                                    PlaceholderManagerImpl.getInstance().parse(owner, text, condition.getArgs())
                                            ),
                                            duration
                                    );
                                }
                            }
                        }, condition.getLocation()
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: hologram");
                return EmptyAction.instance;
            }
        });
    }

    private void registerItemAmountAction() {
        registerAction("item-amount", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                boolean mainOrOff = section.getString("hand", "main").equalsIgnoreCase("main");
                int amount = section.getInt("amount", 1);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    ItemStack itemStack = mainOrOff ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                    itemStack.setAmount(Math.max(0, itemStack.getAmount() + amount));
                };
            } else {
                LogUtils.warn("Illegal value format found at action: item-amount");
                return EmptyAction.instance;
            }
        });
    }

    private void registerItemDurabilityAction() {
        registerAction("durability", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                EquipmentSlot slot = EquipmentSlot.valueOf(section.getString("slot", "hand").toUpperCase(Locale.ENGLISH));
                int amount = section.getInt("amount", 1);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    ItemStack itemStack = player.getInventory().getItem(slot);
                    if (amount > 0) {
                        ItemUtils.increaseDurability(itemStack, amount, true);
                    } else {
                        ItemUtils.decreaseDurability(condition.getPlayer(), itemStack, -amount, true);
                    }
                };
            } else {
                LogUtils.warn("Illegal value format found at action: durability");
                return EmptyAction.instance;
            }
        });
    }

    private void registerGiveItemAction() {
        registerAction("give-item", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String id = section.getString("item");
                int amount = section.getInt("amount", 1);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    ItemUtils.giveCertainAmountOfItem(player, CustomFishingPlugin.get().getItemManager().buildAnyPluginItemByID(player, id), amount);
                };
            } else {
                LogUtils.warn("Illegal value format found at action: give-item");
                return EmptyAction.instance;
            }
        });
    }

    private void registerFakeItemAction() {
        registerAction("fake-item", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String[] itemSplit = section.getString("item", "").split(":", 2);
                int duration = section.getInt("duration", 20);
                boolean position = section.getString("position", "hook").equals("hook");
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    Location location = position ? condition.getLocation() : player.getLocation();
                    ArmorStandUtils.sendFakeItem(
                            condition.getPlayer(),
                            location.clone().add(x, y, z),
                            plugin.getItemManager().build(player, itemSplit[0], itemSplit[1], condition.getArgs()),
                            duration
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: fake-item");
                return EmptyAction.instance;
            }
        });
    }

    private void registerChainAction() {
        registerAction("chain", (args, chance) -> {
            List<Action> actions = new ArrayList<>();
            if (args instanceof ConfigurationSection section) {
                for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    if (entry.getValue() instanceof ConfigurationSection innerSection) {
                        actions.add(getAction(innerSection));
                    }
                }
            }
            return condition -> {
                if (Math.random() > chance) return;
                for (Action action : actions) {
                    action.trigger(condition);
                }
            };
        });
    }

    private void registerMoneyAction() {
        registerAction("give-money", (args, chance) -> {
            var value = ConfigUtils.getValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                VaultHook.getEconomy().depositPlayer(condition.getPlayer(), value.get(condition.getPlayer()));
            };
        });
        registerAction("take-money", (args, chance) -> {
            var value = ConfigUtils.getValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                VaultHook.getEconomy().withdrawPlayer(condition.getPlayer(), value.get(condition.getPlayer()));
            };
        });
    }

    private void registerDelayedAction() {
        registerAction("delay", (args, chance) -> {
            List<Action> actions = new ArrayList<>();
            int delay;
            if (args instanceof ConfigurationSection section) {
                delay = section.getInt("delay", 1);
                ConfigurationSection actionSection = section.getConfigurationSection("actions");
                if (actionSection != null) {
                    for (Map.Entry<String, Object> entry : actionSection.getValues(false).entrySet()) {
                        if (entry.getValue() instanceof ConfigurationSection innerSection) {
                            actions.add(getAction(innerSection));
                        }
                    }
                }
            } else {
                delay = 1;
            }
            return condition -> {
                if (Math.random() > chance) return;
                plugin.getScheduler().runTaskSyncLater(() -> {
                    for (Action action : actions) {
                        action.trigger(condition);
                    }
                }, condition.getLocation(), delay * 50L, TimeUnit.MILLISECONDS);
            };
        });
    }

    private void registerTitleAction() {
        registerAction("title", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String title = section.getString("title");
                String subtitle = section.getString("subtitle");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return condition -> {
                    if (Math.random() > chance) return;
                    AdventureManagerImpl.getInstance().sendTitle(
                            condition.getPlayer(),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), title, condition.getArgs()),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), subtitle, condition.getArgs()),
                            fadeIn,
                            stay,
                            fadeOut
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: title");
                return EmptyAction.instance;
            }
        });
        registerAction("title-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String title = section.getString("title");
                String subtitle = section.getString("subtitle");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                int range = section.getInt("range", 32);
                return condition -> {
                    if (Math.random() > chance) return;
                    plugin.getScheduler().runTaskSync(() -> {
                            for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                                double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                                if (distance <= range) {
                                    condition.insertArg("{near}", player.getName());
                                    AdventureManagerImpl.getInstance().sendTitle(
                                            condition.getPlayer(),
                                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), title, condition.getArgs()),
                                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), subtitle, condition.getArgs()),
                                            fadeIn,
                                            stay,
                                            fadeOut
                                    );
                                    condition.delArg("{near}");
                                }
                            }
                        }, condition.getLocation()
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: title-nearby");
                return EmptyAction.instance;
            }
        });
        registerAction("random-title", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<String> titles = section.getStringList("titles");
                if (titles.size() == 0) titles.add("");
                List<String> subtitles = section.getStringList("subtitles");
                if (subtitles.size() == 0) subtitles.add("");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return condition -> {
                    if (Math.random() > chance) return;
                    AdventureManagerImpl.getInstance().sendTitle(
                            condition.getPlayer(),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), titles.get(ThreadLocalRandom.current().nextInt(titles.size())), condition.getArgs()),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), subtitles.get(ThreadLocalRandom.current().nextInt(subtitles.size())), condition.getArgs()),
                            fadeIn,
                            stay,
                            fadeOut
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: random-title");
                return EmptyAction.instance;
            }
        });
    }

    private void registerPotionAction() {
        registerAction("potion-effect", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                PotionEffect potionEffect = new PotionEffect(
                        Objects.requireNonNull(PotionEffectType.getByName(section.getString("type", "BLINDNESS").toUpperCase(Locale.ENGLISH))),
                        section.getInt("duration", 20),
                        section.getInt("amplifier", 0)
                );
                return condition -> {
                    if (Math.random() > chance) return;
                    condition.getPlayer().addPotionEffect(potionEffect);
                };
            } else {
                LogUtils.warn("Illegal value format found at action: potion-effect");
                return EmptyAction.instance;
            }
        });
    }

    private void registerLevelAction() {
        registerAction("level", (args, chance) -> {
            var value = ConfigUtils.getValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                Player player = condition.getPlayer();
                player.setLevel((int) Math.max(0, player.getLevel() + value.get(condition.getPlayer())));
            };
        });
    }

    @SuppressWarnings("all")
    private void registerSoundAction() {
        registerAction("sound", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                Sound sound = Sound.sound(
                        Key.key(section.getString("key")),
                        Sound.Source.valueOf(section.getString("source", "PLAYER").toUpperCase(Locale.ENGLISH)),
                        (float) section.getDouble("volume", 1),
                        (float) section.getDouble("pitch", 1)
                );
                return condition -> {
                    if (Math.random() > chance) return;
                    AdventureManagerImpl.getInstance().sendSound(condition.getPlayer(), sound);
                };
            } else {
                LogUtils.warn("Illegal value format found at action: sound");
                return EmptyAction.instance;
            }
        });
    }

    private void registerConditionalAction() {
        registerAction("conditional", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                Action[] actions = getActions(section.getConfigurationSection("actions"));
                Requirement[] requirements = plugin.getRequirementManager().getRequirements(section.getConfigurationSection("conditions"), true);
                return condition -> {
                    if (Math.random() > chance) return;
                    if (requirements != null)
                        for (Requirement requirement : requirements) {
                            if (!requirement.isConditionMet(condition)) {
                                return;
                            }
                        }
                    for (Action action : actions) {
                        action.trigger(condition);
                    }
                };
            } else {
                LogUtils.warn("Illegal value format found at action: conditional");
                return EmptyAction.instance;
            }
        });
    }

    private void registerPriorityAction() {
        registerAction("priority", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<Pair<Requirement[], Action[]>> conditionActionPairList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    if (entry.getValue() instanceof ConfigurationSection inner) {
                        Action[] actions = getActions(inner.getConfigurationSection("actions"));
                        Requirement[] requirements = plugin.getRequirementManager().getRequirements(inner.getConfigurationSection("conditions"), false);
                        conditionActionPairList.add(Pair.of(requirements, actions));
                    }
                }
                return condition -> {
                    if (Math.random() > chance) return;
                    outer:
                        for (Pair<Requirement[], Action[]> pair : conditionActionPairList) {
                            if (pair.left() != null)
                                for (Requirement requirement : pair.left()) {
                                    if (!requirement.isConditionMet(condition)) {
                                        continue outer;
                                    }
                                }
                            if (pair.right() != null)
                                for (Action action : pair.right()) {
                                    action.trigger(condition);
                                }
                            return;
                        }
                };
            } else {
                LogUtils.warn("Illegal value format found at action: priority");
                return EmptyAction.instance;
            }
        });
    }

    private void registerPluginExpAction() {
        registerAction("plugin-exp", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String pluginName = section.getString("plugin");
                var value = ConfigUtils.getValue(section.get("exp"));
                String target = section.getString("target");
                return condition -> {
                    if (Math.random() > chance) return;
                    Optional.ofNullable(plugin.getIntegrationManager().getLevelPlugin(pluginName)).ifPresentOrElse(it -> {
                        it.addXp(condition.getPlayer(), target, value.get(condition.getPlayer()));
                    }, () -> LogUtils.warn("Plugin (" + pluginName + "'s) level is not compatible. Please double check if it's a problem caused by pronunciation."));
                };
            } else {
                LogUtils.warn("Illegal value format found at action: plugin-exp");
                return EmptyAction.instance;
            }
        });
    }

    private void registerFishFindAction() {
        registerAction("fish-finder", (args, chance) -> {
            boolean arg = (boolean) args;
            return condition -> {
                if (Math.random() > chance) return;
                condition.insertArg("{lava}", String.valueOf(arg));
                LootManager lootManager = plugin.getLootManager();
                List<String> loots = plugin.getLootManager().getPossibleLootKeys(condition).stream().map(lootManager::getLoot).filter(Objects::nonNull).filter(Loot::showInFinder).map(Loot::getNick).toList();
                StringJoiner stringJoiner = new StringJoiner(CFLocale.MSG_Split_Char);
                for (String loot : loots) {
                    stringJoiner.add(loot);
                }
                condition.delArg("{lava}");
                AdventureManagerImpl.getInstance().sendMessageWithPrefix(condition.getPlayer(), CFLocale.MSG_Possible_Loots + stringJoiner);
            };
        });
    }
}
