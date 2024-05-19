package net.momirealms.customfishing.bukkit.loot;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class BukkitLootManager implements LootManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, Loot> lootMap = new HashMap<>();
    private final HashMap<String, List<String>> groupMembersMap = new HashMap<>();

    public BukkitLootManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    private void loadConfig() {

    }

    @Override
    public void registerLoot(@NotNull final Loot loot) {
        requireNonNull(loot, "loot cannot be null");
        this.lootMap.put(loot.getID(), loot);
        for (String group : loot.lootGroup()) {
            addGroupMember(group, loot.getID());
        }
    }

    private void addGroupMember(String group, String member) {
        List<String> members = groupMembersMap.get(group);
        if (members == null) {
            members = new ArrayList<>(List.of(member));
            groupMembersMap.put(group, members);
        } else {
            members.add(member);
        }
    }

    @NotNull
    @Override
    public List<String> getGroupMembers(String key) {
        return Optional.ofNullable(groupMembersMap.get(key)).orElse(List.of());
    }

    @NotNull
    @Override
    public Optional<Loot> getLoot(String key) {
        return Optional.ofNullable(lootMap.get(key));
    }

    @Override
    public HashMap<String, Double> getLootWithWeight(Context<Player> context) {
        return null;
    }

    @Override
    public Collection<String> getPossibleLootKeys(Context<Player> context) {
        return List.of();
    }

    @NotNull
    @Override
    public Map<String, Double> getPossibleLootKeysWithWeight(Effect effect, Context<Player> context) {
        return Map.of();
    }

    @Nullable
    @Override
    public Loot getNextLoot(Effect effect, Context<Player> context) {
        return null;
    }
}
