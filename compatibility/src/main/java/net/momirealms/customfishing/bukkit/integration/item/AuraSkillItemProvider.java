package net.momirealms.customfishing.bukkit.integration.item;

import dev.aurelium.auraskills.api.ability.Abilities;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import dev.aurelium.auraskills.api.loot.Loot;
import dev.aurelium.auraskills.api.loot.LootContext;
import dev.aurelium.auraskills.api.loot.LootPool;
import dev.aurelium.auraskills.api.loot.LootTable;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.source.SkillSource;
import dev.aurelium.auraskills.api.source.XpSource;
import dev.aurelium.auraskills.api.source.type.FishingXpSource;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.hooks.WorldGuardFlags.FlagKey;
import dev.aurelium.auraskills.bukkit.hooks.WorldGuardHook;
import dev.aurelium.auraskills.bukkit.loot.context.MobContext;
import dev.aurelium.auraskills.bukkit.loot.context.SourceContext;
import dev.aurelium.auraskills.bukkit.loot.type.CommandLoot;
import dev.aurelium.auraskills.bukkit.loot.type.EntityLoot;
import dev.aurelium.auraskills.bukkit.loot.type.ItemLoot;
import dev.aurelium.auraskills.bukkit.source.FishingLeveler;
import dev.aurelium.auraskills.common.commands.CommandExecutor;
import dev.aurelium.auraskills.common.hooks.PlaceholderHook;
import dev.aurelium.auraskills.common.message.MessageKey;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import dev.aurelium.auraskills.slate.text.TextFormatter;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

/*
 *
 * These codes are from AuraSkills, licensed under GPL-3.0
 *
 */
public class AuraSkillItemProvider implements ItemProvider {
    private final AuraSkills plugin;
    private final TextFormatter tf = new TextFormatter();
    private final Random random = new Random();

    public AuraSkillItemProvider() {
        plugin = (AuraSkills) Bukkit.getPluginManager().getPlugin("AuraSkills");
    }

    @Override
    public @NotNull ItemStack buildItem(@NotNull Context<Player> context, @NotNull String id) {
        FishHook fishHook = context.arg(ContextKeys.HOOK_ENTITY);
        ItemStack originalItem;
        if (fishHook != null) {
            Player player = context.holder();
            PlayerInventory inventory = player.getInventory();
            ItemStack rod = inventory.getItemInMainHand();
            if (rod.getType() != Material.FISHING_ROD) {
                rod = inventory.getItemInOffHand();
            }
            if (rod.getType() != Material.FISHING_ROD) {
                originalItem = new ItemStack(Material.COD);
            } else {
                originalItem = SparrowHeart.getInstance().getFishingLoot(context.holder(), fishHook, rod).stream().findFirst().orElse(new ItemStack(Material.COD));
            }
        } else {
            originalItem = new ItemStack(Material.COD);
        }
        var originalSource = plugin.getLevelManager().getLeveler(FishingLeveler.class).getSource(originalItem);

        User user = plugin.getUser(context.holder());
        Skill skill = originalSource != null ? originalSource.skill() : Skills.FISHING;

        LootTable table = plugin.getLootTableManager().getLootTable(skill);
        if (table == null) return originalItem;
        for (LootPool pool : table.getPools()) {
            // Calculate chance for pool
            XpSource source = null;
            double chance = getCommonChance(pool, user);

            LootDropEvent.Cause cause = LootDropEvent.Cause.FISHING_OTHER_LOOT;
            if (pool.getName().equals("rare") && Abilities.TREASURE_HUNTER.isEnabled()) {
                chance = getAbilityModifiedChance(chance, Abilities.TREASURE_HUNTER, user);
                source = getSourceWithLootPool("rare", skill);
            } else if (pool.getName().equals("epic") && Abilities.EPIC_CATCH.isEnabled()) {
                chance = getAbilityModifiedChance(chance, Abilities.EPIC_CATCH, user);
                source = getSourceWithLootPool("epic", skill);
            } else if (originalSource != null) {
                source = originalSource.source();
            }

            if (source == null) continue;

            // Skip if pool has no loot matching the source
            if (isPoolUnobtainable(pool, source)) {
                continue;
            }

            if (random.nextDouble() < chance) { // Pool is selected
                XpSource contextSource = originalSource != null ? originalSource.source() : null;
                Loot selectedLoot = selectLoot(pool, new SourceContext(contextSource));
                // Give loot
                if (selectedLoot == null) { // Continue iterating pools
                    continue;
                }
                if (selectedLoot instanceof ItemLoot itemLoot) {
                    return giveFishingItemLoot(context.holder(), itemLoot, source, skill, table);
                } else if (selectedLoot instanceof CommandLoot commandLoot) {
                    giveCommandLoot(context.holder(), commandLoot, source, skill);
                    return new ItemStack(Material.AIR);
                } else if (selectedLoot instanceof EntityLoot entityLoot && fishHook != null) {
                    giveFishingEntityLoot(context.holder(), entityLoot, source, skill, fishHook);
                    return new ItemStack(Material.AIR);
                }
                return new ItemStack(Material.AIR);
            }
        }

        var skillSource = getSource(originalItem);
        if (skillSource != null) {
            FishingXpSource source = skillSource.source();
            plugin.getLevelManager().addXp(user, skill, source, source.getXp());
        }

        return originalItem;
    }

    protected void giveFishingEntityLoot(Player player, EntityLoot loot, @Nullable XpSource source, Skill skill, FishHook fishHook) {
        Location location = fishHook.getLocation();
        Entity entity = loot.getEntity().spawnEntity(plugin, location);

        if (entity == null) return;

        Float hVelocity = loot.getEntity().getEntityProperties().horizontalVelocity();
        if (hVelocity == null) hVelocity = 1.2f;

        Float vVelocity = loot.getEntity().getEntityProperties().verticalVelocity();
        if (vVelocity == null) vVelocity = 1.3f;

        Vector vector = player.getLocation().subtract(location).toVector().multiply(hVelocity - 1);
        vector.setY((vector.getY() + 0.2) * vVelocity);
        entity.setVelocity(vector);

        attemptSendMessage(player, loot);
        giveXp(player, loot, source, skill);
    }

    @Nullable
    public SkillSource<FishingXpSource> getSource(ItemStack item) {
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            if (plugin.getItemRegistry().passesFilter(item, entry.source().getItem(), entry.skill())) { // Return source that passes item filter
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private FishingXpSource getSourceWithLootPool(String lootPool, Skill skill) {
        for (SkillSource<FishingXpSource> entry : plugin.getSkillManager().getSourcesOfType(FishingXpSource.class)) {
            if (!entry.skill().equals(skill)) continue;

            String candidate = entry.source().getItem().lootPool();
            if (candidate == null) continue;

            if (lootPool.equals(candidate)) {
                return entry.source();
            }
        }
        return null;
    }

    @Override
    public @Nullable String itemID(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public String identifier() {
        return "AuraSkills";
    }

    protected void giveCommandLoot(Player player, CommandLoot loot, @Nullable XpSource source, Skill skill) {
        User user = plugin.getUser(player);
        for (String command : loot.getCommands()) {
            String finalCommand = TextUtil.replace(command, "{player}", player.getName());
            if (plugin.getHookManager().isRegistered(PlaceholderHook.class)) {
                finalCommand = plugin.getHookManager().getHook(PlaceholderHook.class).setPlaceholders(user, finalCommand);
            }
            // Execute command
            CommandExecutor executor = loot.getExecutor();
            if (executor == CommandExecutor.CONSOLE) {
                Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), finalCommand);
            } else if (executor == CommandExecutor.PLAYER) {
                Bukkit.dispatchCommand(player, finalCommand);
            }
        }
        attemptSendMessage(player, loot);
        giveXp(player, loot, source, skill);
    }

    protected ItemStack giveFishingItemLoot(Player player, ItemLoot loot, @Nullable XpSource source, Skill skill, LootTable table) {
        int amount = generateAmount(loot.getMinAmount(), loot.getMaxAmount());
        if (amount == 0) return new ItemStack(Material.AIR);

        ItemStack drop = loot.getItem().supplyItem(plugin, table);
        drop.setAmount(amount);

        attemptSendMessage(player, loot);
        giveXp(player, loot, source, skill);

        return drop;
    }

    private void giveXp(Player player, Loot loot, @Nullable XpSource source, Skill skill) {
        if (plugin.getHookManager().isRegistered(WorldGuardHook.class)) {
            // Check generic xp-gain and skill-specific flags
            if (plugin.getHookManager().getHook(WorldGuardHook.class).isBlocked(player.getLocation(), player, skill)) {
                return;
            }
        }

        User user = plugin.getUser(player);
        Object xpObj = loot.getValues().getOptions().get("xp");

        double xp;
        if (xpObj instanceof Integer) {
            xp = (int) xpObj;
        } else if (xpObj instanceof Double) {
            xp = (double) xpObj;
        } else {
            xp = -1.0;
        }
        if (xp == -1.0 && source != null) { // Xp not specified
            plugin.getLevelManager().addXp(user, skill, source, source.getXp());
        } else if (xp > 0) { // Xp explicitly specified
            plugin.getLevelManager().addXp(user, skill, source, xp);
        }
    }

    private int generateAmount(int minAmount, int maxAmount) {
        return new Random().nextInt(maxAmount - minAmount + 1) + minAmount;
    }

    private void attemptSendMessage(Player player, Loot loot) {
        String message = loot.getValues().getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        User user = plugin.getUser(player);

        Locale locale = user.getLocale();
        // Try to get message as message key
        MessageKey messageKey = MessageKey.of(message);
        String keyedMessage = plugin.getMessageProvider().getOrNull(messageKey, locale);
        if (keyedMessage != null) {
            message = keyedMessage;
        }
        // Replace placeholders
        if (plugin.getHookManager().isRegistered(PlaceholderHook.class)) {
            message = plugin.getHookManager().getHook(PlaceholderHook.class).setPlaceholders(user, message);
        }
        user.sendMessage(tf.toComponent(message));
    }

    public double getCommonChance(LootPool pool, User user) {
        double chancePerLuck = pool.getOption("chance_per_luck", Double.class, 0.0) / 100;
        return pool.getBaseChance() + chancePerLuck * user.getStatLevel(Stats.LUCK);
    }

    public double getAbilityModifiedChance(double chance, Ability ability, User user) {
        // Check option to scale base chance
        if (ability.optionBoolean("scale_base_chance", false)) {
            chance *= 1 + (ability.getValue(user.getAbilityLevel(ability)) / 100);
        } else { // Otherwise add to base chance
            chance += (ability.getValue(user.getAbilityLevel(ability)) / 100);
        }
        return chance;
    }

    protected boolean failsChecks(Player player, Location location) {
        if (player.getGameMode() == GameMode.CREATIVE) { // Only drop loot in survival mode
            return true;
        }

        if (plugin.getWorldManager().isInDisabledWorld(location)) return true;

        if (plugin.getHookManager().isRegistered(WorldGuardHook.class)) {
            return plugin.getHookManager().getHook(WorldGuardHook.class).isBlocked(location, player, FlagKey.CUSTOM_LOOT);
        }
        return false;
    }

    @Nullable
    protected Loot selectLoot(LootPool pool, @NotNull LootContext providedContext) {
        return pool.rollLoot(loot -> {
            if (providedContext instanceof SourceContext sourceContext) {
                Set<LootContext> lootContexts = loot.getValues().getContexts().get("sources");
                // Make sure the loot defines a sources context and the provided context exists
                if (lootContexts != null && sourceContext.source() != null) {
                    boolean matched = false;
                    for (LootContext context : lootContexts) { // Go through LootContext and cast to Source
                        if (context instanceof SourceContext sourceLootContext) {
                            if (sourceLootContext.source().equals(sourceContext.source())) { // Check if source matches one of the contexts
                                matched = true;
                                break;
                            }
                        }
                    }
                    return matched;
                }
            } else if (providedContext instanceof MobContext mobContext) {
                Set<LootContext> lootContexts = loot.getValues().getContexts().get("mobs");
                if (lootContexts != null && mobContext.entityType() != null) {
                    boolean matched = false;
                    for (LootContext context : lootContexts) {
                        if (context instanceof MobContext mobLootContext) {
                            if (mobLootContext.entityType().equals(mobContext.entityType())) {
                                matched = true;
                            }
                        }
                    }
                    return matched;
                }
            }
            return true;
        }).orElse(null);
    }

    protected boolean isPoolUnobtainable(LootPool pool, XpSource source) {
        for (Loot loot : pool.getLoot()) {
            Set<LootContext> contexts = loot.getValues().getContexts().getOrDefault("sources", new HashSet<>());
            // Loot will be reachable if it has no contexts
            if (contexts.isEmpty()) {
                return false;
            }
            // Loot is reachable if at least one context matches the entity type
            for (LootContext context : contexts) {
                if (context instanceof SourceContext sourceContext) {
                    if (sourceContext.source().equals(source)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
