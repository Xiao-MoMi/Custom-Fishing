package net.momirealms.customfishing.bukkit.integration.action;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.core.plugin.context.CommonFunctions;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionFactory;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CEActionExpansion implements ActionFactory<Player> {

    public static void register() {
        BukkitCustomFishingPlugin.getInstance().getActionManager().registerAction(new CEActionExpansion(), "ce-function");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Action<Player> process(Object arg, MathValue<Player> mathValue) {
        if (arg instanceof List<?> list) {
            List<Function<net.momirealms.craftengine.core.plugin.context.Context>> functions = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map functionArguments) {
                    functions.add(CommonFunctions.fromMap(functionArguments));
                }
            }
            return context -> {
                PlayerOptionalContext ctx = PlayerOptionalContext.of(BukkitAdaptors.adapt(context.holder()), ContextHolder.builder());
                for (Function<net.momirealms.craftengine.core.plugin.context.Context> function : functions) {
                    function.run(ctx);
                }
            };
        }
        return context -> {};
    }
}
