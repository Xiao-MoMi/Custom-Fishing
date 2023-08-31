package net.momirealms.customfishing.api;

import net.momirealms.customfishing.api.manager.*;
import net.momirealms.customfishing.api.scheduler.Scheduler;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CustomFishingPlugin extends JavaPlugin {

    protected Scheduler scheduler;
    protected CommandManager commandManager;
    protected VersionManager versionManager;
    protected ItemManager itemManager;
    protected RequirementManager requirementManager;
    protected ActionManager actionManager;
    protected LootManager lootManager;
    protected FishingManager fishingManager;
    protected EffectManager effectManager;
    protected MobManager mobManager;
    protected BlockManager blockManager;
    protected AdventureManager adventure;
    protected BagManager bagManager;
    protected GameManager gameManager;
    protected MarketManager marketManager;
    protected IntegrationManager integrationManager;
    protected CompetitionManager competitionManager;
    protected StorageManager storageManager;
    protected PlaceholderManager placeholderManager;

    private static CustomFishingPlugin instance;

    public CustomFishingPlugin() {
        instance = this;
    }

    public static CustomFishingPlugin get() {
        return instance;
    }

    public static CustomFishingPlugin getInstance() {
        return instance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public RequirementManager getRequirementManager() {
        return requirementManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public MobManager getMobManager() {
        return mobManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public MarketManager getMarketManager() {
        return marketManager;
    }

    public FishingManager getFishingManager() {
        return fishingManager;
    }

    public AdventureManager getAdventure() {
        return adventure;
    }

    public BagManager getBagManager() {
        return bagManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public abstract void reload();

    public abstract YamlConfiguration getConfig(String file);

    public abstract boolean isHookedPluginEnabled(String plugin);

    public CompetitionManager getCompetitionManager() {
        return competitionManager;
    }
}
