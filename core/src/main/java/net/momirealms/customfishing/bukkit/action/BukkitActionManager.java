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

package net.momirealms.customfishing.bukkit.action;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.*;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.misc.placeholder.BukkitPlaceholderManager;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.bukkit.integration.VaultHook;
import net.momirealms.customfishing.bukkit.util.LocationUtils;
import net.momirealms.customfishing.bukkit.util.PlayerUtils;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.ClassUtils;
import net.momirealms.customfishing.common.util.ListUtils;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.customfishing.common.util.RandomUtils;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.armorstand.FakeArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

import static java.util.Objects.requireNonNull;

public class BukkitActionManager implements ActionManager<Player> {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, ActionFactory<Player>> actionFactoryMap = new HashMap<>();
    private static final String EXPANSION_FOLDER = "expansions/action";

    public BukkitActionManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.registerBuiltInActions();
    }

    @Override
    public void disable() {
        this.actionFactoryMap.clear();
    }

    @Override
    public void reload() {
        this.loadExpansions();
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
        if (section == null) return EmptyAction.INSTANCE;
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
    public Action<Player>[] parseActions(Section section) {
        ArrayList<Action<Player>> actionList = new ArrayList<>();
        if (section != null)
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
        this.registerCommandAction();
        this.registerActionBarAction();
        this.registerCloseInvAction();
        this.registerExpAction();
        this.registerFoodAction();
        this.registerChainAction();
        this.registerMoneyAction();
        this.registerItemAction();
        this.registerPotionAction();
        this.registerFishFindAction();
        this.registerPluginExpAction();
        this.registerSoundAction();
        this.registerHologramAction();
        this.registerFakeItemAction();
        this.registerTitleAction();
    }

    private void registerMessageAction() {
        registerAction("message", (args, chance) -> {
            List<String> messages = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                List<String> replaced = plugin.getPlaceholderManager().parse(context.getHolder(), messages, context.placeholderMap());
                Audience audience = plugin.getSenderFactory().getAudience(context.getHolder());
                for (String text : replaced) {
                    audience.sendMessage(AdventureHelper.miniMessage(text));
                }
            };
        });
        registerAction("random-message", (args, chance) -> {
            List<String> messages = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                String random = messages.get(RandomUtils.generateRandomInt(0, messages.size() - 1));
                random = BukkitPlaceholderManager.getInstance().parse(context.getHolder(), random, context.placeholderMap());
                Audience audience = plugin.getSenderFactory().getAudience(context.getHolder());
                audience.sendMessage(AdventureHelper.miniMessage(random));
            };
        });
        registerAction("broadcast", (args, chance) -> {
            List<String> messages = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                List<String> replaced = plugin.getPlaceholderManager().parse(context.getHolder(), messages, context.placeholderMap());
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Audience audience = plugin.getSenderFactory().getAudience(player);
                    for (String text : replaced) {
                        audience.sendMessage(AdventureHelper.miniMessage(text));
                    }
                }
            };
        });
        registerAction("message-nearby", (args, chance) -> {
            if (args instanceof Section section) {
                List<String> messages = ListUtils.toList(section.get("message"));
                MathValue<Player> range = MathValue.auto(section.get("range"));
                return context -> {
                    if (Math.random() > chance) return;
                    double realRange = range.evaluate(context);
                    Player owner = context.getHolder();
                    Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                    plugin.getScheduler().sync().run(() -> {
                        for (Entity player : location.getWorld().getNearbyEntities(location, realRange, realRange, realRange, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), location);
                            if (distance <= realRange) {
                                context.arg(ContextKeys.TEMP_NEAR_PLAYER, player.getName());
                                List<String> replaced = BukkitPlaceholderManager.getInstance().parse(
                                        owner,
                                        messages,
                                        context.placeholderMap()
                                );
                                Audience audience = plugin.getSenderFactory().getAudience(player);
                                for (String text : replaced) {
                                    audience.sendMessage(AdventureHelper.miniMessage(text));
                                }
                            }
                        }
                    }, location);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at message-nearby action which should be Section");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerCommandAction() {
        registerAction("command", (args, chance) -> {
            List<String> commands = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                List<String> replaced = BukkitPlaceholderManager.getInstance().parse(context.getHolder(), commands, context.placeholderMap());
                plugin.getScheduler().sync().run(() -> {
                    for (String text : replaced) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
                    }
                }, context.arg(ContextKeys.LOCATION));
            };
        });
        registerAction("player-command", (args, chance) -> {
            List<String> commands = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                List<String> replaced = BukkitPlaceholderManager.getInstance().parse(context.getHolder(), commands, context.placeholderMap());
                plugin.getScheduler().sync().run(() -> {
                    for (String text : replaced) {
                        context.getHolder().performCommand(text);
                    }
                }, context.arg(ContextKeys.LOCATION));
            };
        });
        registerAction("random-command", (args, chance) -> {
            List<String> commands = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                String random = commands.get(ThreadLocalRandom.current().nextInt(commands.size()));
                random = BukkitPlaceholderManager.getInstance().parse(context.getHolder(), random, context.placeholderMap());
                String finalRandom = random;
                plugin.getScheduler().sync().run(() -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalRandom);
                }, context.arg(ContextKeys.LOCATION));
            };
        });
        registerAction("command-nearby", (args, chance) -> {
            if (args instanceof Section section) {
                List<String> cmd = ListUtils.toList(section.get("command"));
                MathValue<Player> range = MathValue.auto(section.get("range"));
                return context -> {
                    if (Math.random() > chance) return;
                    Player owner = context.getHolder();
                    double realRange = range.evaluate(context);
                    Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                    plugin.getScheduler().sync().run(() -> {
                        for (Entity player : location.getWorld().getNearbyEntities(location, realRange, realRange, realRange, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), location);
                            if (distance <= realRange) {
                                context.arg(ContextKeys.TEMP_NEAR_PLAYER, player.getName());
                                List<String> replaced = BukkitPlaceholderManager.getInstance().parse(owner, cmd, context.placeholderMap());
                                for (String text : replaced) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
                                }
                            }
                        }
                    }, location);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at command-nearby action which should be Section");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerCloseInvAction() {
        registerAction("close-inv", (args, chance) -> condition -> {
            if (Math.random() > chance) return;
            condition.getHolder().closeInventory();
        });
    }

    private void registerActionBarAction() {
        registerAction("actionbar", (args, chance) -> {
            String text = (String) args;
            return context -> {
                if (Math.random() > chance) return;
                Audience audience = plugin.getSenderFactory().getAudience(context.getHolder());
                Component component = AdventureHelper.miniMessage(plugin.getPlaceholderManager().parse(context.getHolder(), text, context.placeholderMap()));
                audience.sendActionBar(component);
            };
        });
        registerAction("random-actionbar", (args, chance) -> {
            List<String> texts = ListUtils.toList(args);
            return context -> {
                if (Math.random() > chance) return;
                String random = texts.get(RandomUtils.generateRandomInt(0, texts.size() - 1));
                random = plugin.getPlaceholderManager().parse(context.getHolder(), random, context.placeholderMap());
                Audience audience = plugin.getSenderFactory().getAudience(context.getHolder());
                audience.sendActionBar(AdventureHelper.miniMessage(random));
            };
        });
        registerAction("actionbar-nearby", (args, chance) -> {
            if (args instanceof Section section) {
                String actionbar = section.getString("actionbar");
                MathValue<Player> range = MathValue.auto(section.get("range"));
                return context -> {
                    if (Math.random() > chance) return;
                    Player owner = context.getHolder();
                    Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                    double realRange = range.evaluate(context);
                    plugin.getScheduler().sync().run(() -> {
                                for (Entity player : location.getWorld().getNearbyEntities(location, realRange, realRange, realRange, entity -> entity instanceof Player)) {
                                    double distance = LocationUtils.getDistance(player.getLocation(), location);
                                    if (distance <= realRange) {
                                        context.arg(ContextKeys.TEMP_NEAR_PLAYER, player.getName());
                                        String replaced = plugin.getPlaceholderManager().parse(owner, actionbar, context.placeholderMap());
                                        Audience audience = plugin.getSenderFactory().getAudience(player);
                                        audience.sendActionBar(AdventureHelper.miniMessage(replaced));
                                    }
                                }
                            }, location
                    );
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at actionbar-nearby action which should be Section");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerExpAction() {
        registerAction("mending", (args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() > chance) return;
                final Player player = context.getHolder();
                player.getLocation().getWorld().spawn(player.getLocation().clone().add(0,0.5,0), ExperienceOrb.class, e -> e.setExperience((int) value.evaluate(context)));
            };
        });
        registerAction("exp", (args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() > chance) return;
                final Player player = context.getHolder();
                player.giveExp((int) Math.round(value.evaluate(context)));
                Audience audience = plugin.getSenderFactory().getAudience(player);
                AdventureHelper.playSound(audience, Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.PLAYER, 1, 1));
            };
        });
        registerAction("level", (args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() > chance) return;
                Player player = context.getHolder();
                player.setLevel((int) Math.max(0, player.getLevel() + value.evaluate(context)));
            };
        });
    }

    private void registerFoodAction() {
        registerAction("food", (args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() > chance) return;
                Player player = context.getHolder();
                player.setFoodLevel((int) (player.getFoodLevel() + value.evaluate(context)));
            };
        });
        registerAction("saturation", (args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() > chance) return;
                Player player = context.getHolder();
                player.setSaturation((float) (player.getSaturation() + value.evaluate(context)));
            };
        });
    }

    private void registerItemAction() {
        registerAction("item-amount", (args, chance) -> {
            if (args instanceof Section section) {
                boolean mainOrOff = section.getString("hand", "main").equalsIgnoreCase("main");
                int amount = section.getInt("amount", 1);
                return context -> {
                    if (Math.random() > chance) return;
                    Player player = context.getHolder();
                    ItemStack itemStack = mainOrOff ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                    itemStack.setAmount(Math.max(0, itemStack.getAmount() + amount));
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at item-amount action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
        registerAction("durability", (args, chance) -> {
            if (args instanceof Section section) {
                EquipmentSlot slot = EquipmentSlot.valueOf(section.getString("slot", "hand").toUpperCase(Locale.ENGLISH));
                int amount = section.getInt("amount", 1);
                return context -> {
                    if (Math.random() > chance) return;
                    Player player = context.getHolder();
                    ItemStack itemStack = player.getInventory().getItem(slot);
//                    if (amount > 0) {
//                        ItemUtils.increaseDurability(itemStack, amount, true);
//                    } else {
//                        ItemUtils.decreaseDurability(context.getHolder(), itemStack, -amount, true);
//                    }
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at durability action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
        registerAction("give-item", (args, chance) -> {
            if (args instanceof Section section) {
                String id = section.getString("item");
                int amount = section.getInt("amount", 1);
                return context -> {
                    if (Math.random() > chance) return;
                    Player player = context.getHolder();
                    ItemStack itemStack = plugin.getItemManager().buildAny(context, id);
                    int maxStack = itemStack.getType().getMaxStackSize();
                    int amountToGive = amount;
                    while (amountToGive > 0) {
                        int perStackSize = Math.min(maxStack, amountToGive);
                        amountToGive -= perStackSize;
                        ItemStack more = itemStack.clone();
                        more.setAmount(perStackSize);
                        PlayerUtils.dropItem(player, itemStack, true, true, false);
                    }
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at give-item action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerChainAction() {
        registerAction("chain", (args, chance) -> {
            List<Action<Player>> actions = new ArrayList<>();
            if (args instanceof Section section) {
                for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                    if (entry.getValue() instanceof Section innerSection) {
                        actions.add(parseAction(innerSection));
                    }
                }
            }
            return context -> {
                if (Math.random() > chance) return;
                for (Action<Player> action : actions) {
                    action.trigger(context);
                }
            };
        });
        registerAction("delay", (args, chance) -> {
            List<Action<Player>> actions = new ArrayList<>();
            int delay;
            boolean async;
            if (args instanceof Section section) {
                delay = section.getInt("delay", 1);
                async = section.getBoolean("async", false);
                Section actionSection = section.getSection("actions");
                if (actionSection != null)
                    for (Map.Entry<String, Object> entry : actionSection.getStringRouteMappedValues(false).entrySet())
                        if (entry.getValue() instanceof Section innerSection)
                            actions.add(parseAction(innerSection));
            } else {
                delay = 1;
                async = false;
            }
            return context -> {
                if (Math.random() > chance) return;
                Location location = context.arg(ContextKeys.LOCATION);
                if (async) {
                    plugin.getScheduler().asyncLater(() -> {
                        for (Action<Player> action : actions)
                            action.trigger(context);
                    }, delay * 50L, TimeUnit.MILLISECONDS);
                } else {
                    plugin.getScheduler().sync().runLater(() -> {
                        for (Action<Player> action : actions)
                            action.trigger(context);
                    }, delay, location);
                }
            };
        });
        registerAction("timer", (args, chance) -> {
            List<Action<Player>> actions = new ArrayList<>();
            int delay, duration, period;
            boolean async;
            if (args instanceof Section section) {
                delay = section.getInt("delay", 2);
                duration = section.getInt("duration", 20);
                period = section.getInt("period", 2);
                async = section.getBoolean("async", false);
                Section actionSection = section.getSection("actions");
                if (actionSection != null)
                    for (Map.Entry<String, Object> entry : actionSection.getStringRouteMappedValues(false).entrySet())
                        if (entry.getValue() instanceof Section innerSection)
                            actions.add(parseAction(innerSection));
            } else {
                delay = 1;
                period = 1;
                async = false;
                duration = 20;
            }
            return context -> {
                if (Math.random() > chance) return;
                Location location = context.arg(ContextKeys.LOCATION);
                SchedulerTask task;
                if (async) {
                    task = plugin.getScheduler().asyncRepeating(() -> {
                        for (Action<Player> action : actions) {
                            action.trigger(context);
                        }
                    }, delay * 50L, period * 50L, TimeUnit.MILLISECONDS);
                } else {
                    task = plugin.getScheduler().sync().runRepeating(() -> {
                        for (Action<Player> action : actions) {
                            action.trigger(context);
                        }
                    }, delay, period, location);
                }
                plugin.getScheduler().asyncLater(task::cancel, duration * 50L, TimeUnit.MILLISECONDS);
            };
        });
        registerAction("conditional", (args, chance) -> {
            if (args instanceof Section section) {
                Action<Player>[] actions = parseActions(section.getSection("actions"));
                Requirement<Player>[] requirements = plugin.getRequirementManager().parseRequirements(section.getSection("conditions"), true);
                return condition -> {
                    if (Math.random() > chance) return;
                    for (Requirement<Player> requirement : requirements) {
                        if (!requirement.isSatisfied(condition)) {
                            return;
                        }
                    }
                    for (Action<Player> action : actions) {
                        action.trigger(condition);
                    }
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at conditional action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
        registerAction("priority", (args, chance) -> {
            if (args instanceof Section section) {
                List<Pair<Requirement<Player>[], Action<Player>[]>> conditionActionPairList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
                    if (entry.getValue() instanceof Section inner) {
                        Action<Player>[] actions = parseActions(inner.getSection("actions"));
                        Requirement<Player>[] requirements = plugin.getRequirementManager().parseRequirements(inner.getSection("conditions"), false);
                        conditionActionPairList.add(Pair.of(requirements, actions));
                    }
                }
                return context -> {
                    if (Math.random() > chance) return;
                    outer:
                    for (Pair<Requirement<Player>[], Action<Player>[]> pair : conditionActionPairList) {
                        if (pair.left() != null)
                            for (Requirement<Player> requirement : pair.left()) {
                                if (!requirement.isSatisfied(context)) {
                                    continue outer;
                                }
                            }
                        if (pair.right() != null)
                            for (Action<Player> action : pair.right()) {
                                action.trigger(context);
                            }
                        return;
                    }
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at priority action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerMoneyAction() {
        registerAction("give-money", (args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() > chance) return;
                VaultHook.deposit(context.getHolder(), value.evaluate(context));
            };
        });
        registerAction("take-money", (args, chance) -> {
            MathValue<Player> value = MathValue.auto(args);
            return context -> {
                if (Math.random() > chance) return;
                VaultHook.withdraw(context.getHolder(), value.evaluate(context));
            };
        });
    }

    private void registerPotionAction() {
        registerAction("potion-effect", (args, chance) -> {
            if (args instanceof Section section) {
                PotionEffect potionEffect = new PotionEffect(
                        Objects.requireNonNull(PotionEffectType.getByName(section.getString("type", "BLINDNESS").toUpperCase(Locale.ENGLISH))),
                        section.getInt("duration", 20),
                        section.getInt("amplifier", 0)
                );
                return context -> {
                    if (Math.random() > chance) return;
                    context.getHolder().addPotionEffect(potionEffect);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at potion-effect action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerSoundAction() {
        registerAction("sound", (args, chance) -> {
            if (args instanceof Section section) {
                Sound sound = Sound.sound(
                        Key.key(section.getString("key")),
                        Sound.Source.valueOf(section.getString("source", "PLAYER").toUpperCase(Locale.ENGLISH)),
                        section.getDouble("volume", 1.0).floatValue(),
                        section.getDouble("pitch", 1.0).floatValue()
                );
                return context -> {
                    if (Math.random() > chance) return;
                    Audience audience = plugin.getSenderFactory().getAudience(context.getHolder());
                    AdventureHelper.playSound(audience, sound);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at sound action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
    }


    private void registerPluginExpAction() {
        registerAction("plugin-exp", (args, chance) -> {
            if (args instanceof Section section) {
                String pluginName = section.getString("plugin");
                MathValue<Player> value = MathValue.auto(section.get("exp"));
                String target = section.getString("target");
                return context -> {
                    if (Math.random() > chance) return;
                    Optional.ofNullable(plugin.getIntegrationManager().getLevelerProvider(pluginName)).ifPresentOrElse(it -> {
                        it.addXp(context.getHolder(), target, value.evaluate(context));
                    }, () -> plugin.getPluginLogger().warn("Plugin (" + pluginName + "'s) level is not compatible. Please double check if it's a problem caused by pronunciation."));
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at plugin-exp action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerTitleAction() {
        registerAction("title", (args, chance) -> {
            if (args instanceof Section section) {
                TextValue<Player> title = TextValue.auto(section.getString("title", ""));
                TextValue<Player> subtitle = TextValue.auto(section.getString("subtitle", ""));
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return context -> {
                    if (Math.random() > chance) return;
                    final Player player = context.getHolder();
                    Audience audience = plugin.getSenderFactory().getAudience(player);
                    AdventureHelper.sendTitle(audience,
                            AdventureHelper.miniMessage(title.render(context)),
                            AdventureHelper.miniMessage(subtitle.render(context)),
                            fadeIn, stay, fadeOut
                    );
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at title action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
        registerAction("random-title", (args, chance) -> {
            if (args instanceof Section section) {
                List<String> titles = section.getStringList("titles");
                if (titles.isEmpty()) titles.add("");
                List<String> subtitles = section.getStringList("subtitles");
                if (subtitles.isEmpty()) subtitles.add("");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return context -> {
                    if (Math.random() > chance) return;
                    TextValue<Player> title = TextValue.auto(titles.get(RandomUtils.generateRandomInt(0, titles.size() - 1)));
                    TextValue<Player> subtitle = TextValue.auto(subtitles.get(RandomUtils.generateRandomInt(0, subtitles.size() - 1)));
                    final Player player = context.getHolder();
                    Audience audience = plugin.getSenderFactory().getAudience(player);
                    AdventureHelper.sendTitle(audience,
                            AdventureHelper.miniMessage(title.render(context)),
                            AdventureHelper.miniMessage(subtitle.render(context)),
                            fadeIn, stay, fadeOut
                    );
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at random-title action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
        registerAction("title-nearby", (args, chance) -> {
            if (args instanceof Section section) {
                TextValue<Player> title = TextValue.auto(section.getString("title"));
                TextValue<Player> subtitle = TextValue.auto(section.getString("subtitle"));
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                int range = section.getInt("range", 0);
                return context -> {
                    if (Math.random() > chance) return;
                    Location location = requireNonNull(context.arg(ContextKeys.LOCATION));
                    plugin.getScheduler().sync().run(() -> {
                            for (Entity player : location.getWorld().getNearbyEntities(location, range, range, range, entity -> entity instanceof Player)) {
                                double distance = LocationUtils.getDistance(player.getLocation(), location);
                                if (distance <= range) {
                                    context.arg(ContextKeys.TEMP_NEAR_PLAYER, player.getName());
                                    Audience audience = plugin.getSenderFactory().getAudience(player);
                                    AdventureHelper.sendTitle(audience,
                                            AdventureHelper.miniMessage(title.render(context)),
                                            AdventureHelper.miniMessage(subtitle.render(context)),
                                            fadeIn, stay, fadeOut
                                    );
                                }
                            }
                        }, location
                    );
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at title-nearby action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        });
    }

    private void registerFakeItemAction() {
        registerAction("fake-item", ((args, chance) -> {
            if (args instanceof Section section) {
                String itemID = section.getString("item", "");
                String[] split = itemID.split(":");
                if (split.length >= 2) itemID = split[split.length - 1];
                MathValue<Player> duration = MathValue.auto(section.get("duration", 20));
                boolean position = !section.getString("position", "player").equals("player");
                MathValue<Player> x = MathValue.auto(section.get("x", 0));
                MathValue<Player> y = MathValue.auto(section.get("y", 0));
                MathValue<Player> z = MathValue.auto(section.get("z", 0));
                MathValue<Player> yaw = MathValue.auto(section.get("yaw", 0));
                int range = section.getInt("range", 0);
                boolean opposite = section.getBoolean("opposite-yaw", false);
                String finalItemID = itemID;
                return context -> {
                    if (Math.random() > chance) return;
                    Player owner = context.getHolder();
                    Location location = position ? requireNonNull(context.arg(ContextKeys.OTHER_LOCATION)).clone() : owner.getLocation().clone();
                    location.add(x.evaluate(context), y.evaluate(context) - 1, z.evaluate(context));
                    if (opposite) location.setYaw(-owner.getLocation().getYaw());
                    else location.setYaw((float) yaw.evaluate(context));
                    FakeArmorStand armorStand = SparrowHeart.getInstance().createFakeArmorStand(location);
                    armorStand.invisible(true);
                    armorStand.equipment(EquipmentSlot.HEAD, plugin.getItemManager().buildInternal(context, finalItemID));
                    ArrayList<Player> viewers = new ArrayList<>();
                    if (range > 0) {
                        for (Entity player : location.getWorld().getNearbyEntities(location, range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), location);
                            if (distance <= range) {
                                viewers.add((Player) player);
                            }
                        }
                    } else {
                        viewers.add(owner);
                    }
                    for (Player player : viewers) {
                        armorStand.spawn(player);
                    }
                    plugin.getScheduler().asyncLater(() -> {
                        for (Player player : viewers) {
                            if (player.isOnline() && player.isValid()) {
                                armorStand.destroy(player);
                            }
                        }
                    }, (long) (duration.evaluate(context) * 50), TimeUnit.MILLISECONDS);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at fake-item action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        }));
    }

    private void registerHologramAction() {
        registerAction("hologram", ((args, chance) -> {
            if (args instanceof Section section) {
                TextValue<Player> text = TextValue.auto(section.getString("text", ""));
                MathValue<Player> duration = MathValue.auto(section.get("duration", 20));
                boolean position = section.getString("position", "other").equals("other");
                MathValue<Player> x = MathValue.auto(section.get("x", 0));
                MathValue<Player> y = MathValue.auto(section.get("y", 0));
                MathValue<Player> z = MathValue.auto(section.get("z", 0));
                int range = section.getInt("range", 16);
                return context -> {
                    if (Math.random() > chance) return;
                    Player owner = context.getHolder();
                    Location location = position ? requireNonNull(context.arg(ContextKeys.OTHER_LOCATION)).clone() : owner.getLocation().clone();
                    location.add(x.evaluate(context), y.evaluate(context), z.evaluate(context));
                    FakeArmorStand armorStand = SparrowHeart.getInstance().createFakeArmorStand(location);
                    armorStand.invisible(true);
                    armorStand.small(true);
                    armorStand.name(AdventureHelper.miniMessageToJson(text.render(context)));
                    ArrayList<Player> viewers = new ArrayList<>();
                    if (range > 0) {
                        for (Entity player : location.getWorld().getNearbyEntities(location, range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), location);
                            if (distance <= range) {
                                viewers.add((Player) player);
                            }
                        }
                    } else {
                        viewers.add(owner);
                    }
                    for (Player player : viewers) {
                        armorStand.spawn(player);
                    }
                    plugin.getScheduler().asyncLater(() -> {
                        for (Player player : viewers) {
                            if (player.isOnline() && player.isValid()) {
                                armorStand.destroy(player);
                            }
                        }
                    }, (long) (duration.evaluate(context) * 50), TimeUnit.MILLISECONDS);
                };
            } else {
                plugin.getPluginLogger().warn("Invalid value type: " + args.getClass().getSimpleName() + " found at hologram action which is expected to be `Section`");
                return EmptyAction.INSTANCE;
            }
        }));
    }

    private void registerFishFindAction() {
        registerAction("fish-finder", (args, chance) -> {
            String surrounding = (String) args;
            return context -> {
                if (Math.random() > chance) return;
                String previous = context.arg(ContextKeys.SURROUNDING);
                context.arg(ContextKeys.SURROUNDING, surrounding);
                Collection<String> loots = plugin.getLootManager().getWeightedLoots(Effect.newInstance(), context).keySet();
                StringJoiner stringJoiner = new StringJoiner(TranslationManager.miniMessageTranslation(MessageConstants.COMMAND_FISH_FINDER_SPLIT_CHAR.build().key()));
                for (String loot : loots) {
                    plugin.getLootManager().getLoot(loot).ifPresent(lootIns -> {
                        if (lootIns.showInFinder()) {
                            if (!lootIns.nick().equals("UNDEFINED")) {
                                stringJoiner.add(lootIns.nick());
                            }
                        }
                    });
                }
                if (previous == null) {
                    context.remove(ContextKeys.SURROUNDING);
                } else {
                    context.arg(ContextKeys.SURROUNDING, previous);
                }
                if (loots.isEmpty()) {
                    plugin.getSenderFactory().wrap(context.getHolder()).sendMessage(TranslationManager.render(MessageConstants.COMMAND_FISH_FINDER_NO_LOOT.build()));
                } else {
                    plugin.getSenderFactory().wrap(context.getHolder()).sendMessage(TranslationManager.render(MessageConstants.COMMAND_FISH_FINDER_POSSIBLE_LOOTS.arguments(AdventureHelper.miniMessage(stringJoiner.toString())).build()));
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
