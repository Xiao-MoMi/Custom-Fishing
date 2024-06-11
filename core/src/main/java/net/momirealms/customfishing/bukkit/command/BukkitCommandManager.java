package net.momirealms.customfishing.bukkit.command;

import net.kyori.adventure.util.Index;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.feature.*;
import net.momirealms.customfishing.common.command.AbstractCommandManager;
import net.momirealms.customfishing.common.command.CommandFeature;
import net.momirealms.customfishing.common.sender.Sender;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.incendo.cloud.setting.ManagerSetting;

import java.util.List;

public class BukkitCommandManager extends AbstractCommandManager<CommandSender> {

    private final List<CommandFeature<CommandSender>> FEATURES = List.of(
            new ReloadCommand(this),
            new SellFishCommand(this),
            new GetItemCommand(this),
            new GiveItemCommand(this),
            new EndCompetitionCommand(this),
            new StopCompetitionCommand(this),
            new StartCompetitionCommand(this),
            new OpenMarketCommand(this),
            new OpenBagCommand(this)
    );

    private final Index<String, CommandFeature<CommandSender>> INDEX = Index.create(CommandFeature::getFeatureID, FEATURES);

    public BukkitCommandManager(BukkitCustomFishingPlugin plugin) {
        super(plugin, new LegacyPaperCommandManager<>(
                plugin.getBoostrap(),
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity()
        ));
        final LegacyPaperCommandManager<CommandSender> manager = (LegacyPaperCommandManager<CommandSender>) getCommandManager();
        manager.settings().set(ManagerSetting.ALLOW_UNSAFE_REGISTRATION, true);
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
            manager.brigadierManager().setNativeNumberSuggestions(true);
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
    }

    @Override
    protected Sender wrapSender(CommandSender sender) {
        return ((BukkitCustomFishingPlugin) plugin).getSenderFactory().wrap(sender);
    }

    @Override
    public Index<String, CommandFeature<CommandSender>> getFeatures() {
        return INDEX;
    }
}
