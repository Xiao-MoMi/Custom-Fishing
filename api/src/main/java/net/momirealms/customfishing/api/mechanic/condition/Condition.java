package net.momirealms.customfishing.api.mechanic.condition;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Condition {

    @Nullable
    protected final Location location;
    @Nullable
    protected final Player player;
    @NotNull
    protected final Map<String, String> args;

    public Condition() {
        this(null, null, new HashMap<>());
    }

    public Condition(HashMap<String, String> args) {
        this(null, null, args);
    }

    public Condition(Player player) {
        this(player.getLocation(), player, new HashMap<>());
    }

    public Condition(Player player, Map<String, String> args) {
        this(player.getLocation(), player, args);
    }

    public Condition(@Nullable Location location, @Nullable Player player, @NotNull Map<String, String> args) {
        this.location = location;
        this.player = player;
        this.args = args;
        if (player != null)
            this.args.put("{player}", player.getName());
        if (location != null) {
            this.args.put("{x}", String.valueOf(location.getX()));
            this.args.put("{y}", String.valueOf(location.getY()));
            this.args.put("{z}", String.valueOf(location.getZ()));
            this.args.put("{world}", location.getWorld().getName());
        }
    }

    @Nullable
    public Location getLocation() {
        return location;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Map<String, String> getArgs() {
        return args;
    }

    @Nullable
    public String getArg(String key) {
        return args.get(key);
    }

    public void insertArg(String key, String value) {
        args.put(key, value);
    }
}
