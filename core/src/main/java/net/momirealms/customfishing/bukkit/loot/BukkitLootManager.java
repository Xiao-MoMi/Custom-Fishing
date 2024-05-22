package net.momirealms.customfishing.bukkit.loot;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BukkitLootManager implements LootManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, Loot> lootMap = new HashMap<>();
    private final HashMap<String, List<String>> groupMembersMap = new HashMap<>();

    public BukkitLootManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerLoot(@NotNull Loot loot) {
        this.lootMap.put(loot.id(), loot);
        for (String group : loot.lootGroup()) {
            addGroupMember(group, loot.id());
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
    public HashMap<String, Double> getWeightedLoots(Context<Player> context) {
        return null;
    }

    @Nullable
    @Override
    public Loot getNextLoot(Effect effect, Context<Player> context) {
        return null;
    }
}
