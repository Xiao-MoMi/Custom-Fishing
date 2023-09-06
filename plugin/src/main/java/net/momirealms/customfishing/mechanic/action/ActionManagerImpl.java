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
import net.momirealms.customfishing.api.manager.ActionManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionExpansion;
import net.momirealms.customfishing.api.mechanic.action.ActionFactory;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.util.ClassUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private final String EXPANSION_FOLDER = "expansions/actions";

    public ActionManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.actionBuilderMap = new HashMap<>();
        this.registerInbuiltActions();
    }

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
    }

    public void load() {
        this.loadExpansions();
    }

    public void unload() {

    }

    public void disable() {
        this.actionBuilderMap.clear();
    }

    @Override
    public boolean registerAction(String type, ActionFactory actionFactory) {
        if (this.actionBuilderMap.containsKey(type)) return false;
        this.actionBuilderMap.put(type, actionFactory);
        return true;
    }

    @Override
    public boolean unregisterAction(String type) {
        return this.actionBuilderMap.remove(type) != null;
    }

    @Override
    public Action getAction(ConfigurationSection section) {
        return getActionBuilder(section.getString("type")).build(section.get("value"), section.getDouble("chance", 1d));
    }

    @Nullable
    @Override
    public Action[] getActions(ConfigurationSection section) {
        if (section == null) return null;
        ArrayList<Action> actionList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                actionList.add(getAction(innerSection));
            }
        }
        return actionList.toArray(new Action[0]);
    }

    @Override
    public ActionFactory getActionBuilder(String type) {
        return actionBuilderMap.get(type);
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
    }

    private void registerMendingAction() {
        registerAction("mending", (args, chance) -> {
            int xp = (int) args;
            return condition -> {
                if (Math.random() > chance) return;
                if (CustomFishingPlugin.get().getVersionManager().isSpigot()) {
                    condition.getPlayer().getLocation().getWorld().spawn(condition.getPlayer().getLocation(), ExperienceOrb.class, e -> e.setExperience(xp));
                } else {
                    condition.getPlayer().giveExp(xp, true);
                    AdventureManagerImpl.getInstance().sendSound(condition.getPlayer(), Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
                }
            };
        });
    }

    private void registerExpAction() {
        registerAction("exp", (args, chance) -> {
            int xp = (int) args;
            return condition -> {
                if (Math.random() > chance) return;
                condition.getPlayer().giveExp(xp);
            };
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

    private void registerDelayedAction() {
        registerAction("delay", (args, chance) -> {
            List<Action> actions = new ArrayList<>();
            int delay;
            if (args instanceof ConfigurationSection section) {
                delay = section.getInt("delay", 1);
                ConfigurationSection actionSection = section.getConfigurationSection("action");
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
                            fadeIn * 50,
                            stay * 50,
                            fadeOut * 50
                    );
                };
            }
            return null;
        });
        registerAction("random-title", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<String> titles = section.getStringList("titles");
                List<String> subtitles = section.getStringList("subtitles");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return condition -> {
                    if (Math.random() > chance) return;
                    AdventureManagerImpl.getInstance().sendTitle(
                            condition.getPlayer(),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), titles.get(ThreadLocalRandom.current().nextInt(titles.size())), condition.getArgs()),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), subtitles.get(ThreadLocalRandom.current().nextInt(subtitles.size())), condition.getArgs()),
                            fadeIn * 50,
                            stay * 50,
                            fadeOut * 50
                    );
                };
            }
            return null;
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
            }
            LogUtils.warn("Illegal value format found at action: potion-effect");
            return null;
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
            }
            LogUtils.warn("Illegal value format found at action: sound");
            return null;
        });
    }

    private void registerPluginExpAction() {
        registerAction("plugin-exp", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String pluginName = section.getString("plugin");
                double exp = section.getDouble("exp", 1);
                String target = section.getString("target");
                return condition -> {
                    if (Math.random() > chance) return;
                    Optional.ofNullable(plugin.getIntegrationManager().getLevelHook(pluginName)).ifPresentOrElse(it -> {
                        it.addXp(condition.getPlayer(), target, exp);
                    }, () -> LogUtils.warn("Plugin (" + pluginName + "'s) level is not compatible. Please double check if it's a problem caused by pronunciation."));
                };
            }
            return null;
        });
    }

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
}
