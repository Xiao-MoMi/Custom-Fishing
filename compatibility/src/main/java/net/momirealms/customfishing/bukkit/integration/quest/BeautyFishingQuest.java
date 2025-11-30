package net.momirealms.customfishing.bukkit.integration.quest;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.StageTypeRegistry;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractItemStage;
import fr.skytasul.quests.api.utils.CountableObject;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static fr.skytasul.quests.api.gui.ItemUtils.item;

public class BeautyFishingQuest {

    public static void register() {
        StageTypeRegistry stages = QuestsAPI.getAPI().getStages();
        stages.register(new StageType<>("CUSTOMFISHING", CustomFishingStage.class, "CustomFishing",
                CustomFishingStage::deserialize, item(XMaterial.TROPICAL_FISH, "§bCustomFishing"), CustomFishingStage.Creator::new));
    }

    public static class CustomFishingStage extends AbstractItemStage implements Listener {
        protected CustomFishingStage(@NotNull StageController controller, @NotNull List<@NotNull CountableObject<ItemStack>> objects, ItemComparisonMap comparisons) {
            super(controller, objects, comparisons);
        }

        public CustomFishingStage(StageController controller, ConfigurationSection section) {
            super(controller, section);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onFish(FishingLootSpawnEvent e) {
            if (e.getEntity() instanceof Item item) {
                Player p = e.getPlayer();
                event(p, item.getItemStack(), item.getItemStack().getAmount());
            }
        }

        @Override
        public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
            return "CustomFishing Loots";
        }

        public static CustomFishingStage deserialize(ConfigurationSection section, StageController controller) {
            return new CustomFishingStage(controller, section);
        }

        public static class Creator extends AbstractItemStage.Creator<CustomFishingStage> {
            private static final ItemStack editFishesItem = item(XMaterial.FISHING_ROD, Lang.editFishes.toString());

            public Creator(@NotNull StageCreationContext<CustomFishingStage> context) {
                super(context);
            }

            @Override
            protected @NotNull ItemStack getEditItem() {
                return editFishesItem;
            }

            @Override
            protected CustomFishingStage finishStage(@NotNull StageController controller, @NotNull List<CountableObject<ItemStack>> items, @NotNull ItemComparisonMap comparisons) {
                return new CustomFishingStage(controller, items, comparisons);
            }
        }
    }
}
