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

package net.momirealms.customfishing.setting;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CFLocale {
    public static String MSG_Total_Size;
    public static String MSG_Catch_Amount;
    public static String MSG_Total_Score;
    public static String MSG_Max_Size;
    public static String MSG_No_Player;
    public static String MSG_No_Score;
    public static String MSG_Prefix;
    public static String MSG_Reload;
    public static String MSG_Competition_Not_Exist;
    public static String MSG_No_Competition_Ongoing;
    public static String MSG_End_Competition;
    public static String MSG_Stop_Competition;
    public static String MSG_No_Rank;
    public static String MSG_Item_Not_Exists;
    public static String MSG_Get_Item;
    public static String MSG_Give_Item;
    public static String MSG_Never_Played;
    public static String MSG_Unsafe_Modification;
    public static String MSG_Data_Not_Loaded;
    public static String MSG_Market_GUI_Open;
    public static String MSG_Fishing_Bag_Open;
    public static String MSG_Split_Char;
    public static String MSG_Possible_Loots;
    public static String FORMAT_Day;
    public static String FORMAT_Hour;
    public static String FORMAT_Minute;
    public static String FORMAT_Second;
    public static String GUI_SCROLL_DOWN;
    public static String GUI_SCROLL_UP;
    public static String GUI_CANNOT_SCROLL_UP;
    public static String GUI_CANNOT_SCROLL_DOWN;
    public static String GUI_NEXT_PAGE;
    public static String GUI_GOTO_NEXT_PAGE;
    public static String GUI_CANNOT_GOTO_NEXT_PAGE;
    public static String GUI_PREVIOUS_PAGE;
    public static String GUI_GOTO_PREVIOUS_PAGE;
    public static String GUI_CANNOT_GOTO_PREVIOUS_PAGE;
    public static String GUI_BACK_TO_PARENT_PAGE;
    public static String GUI_BACK_TO_PARENT_FOLDER;
    public static String GUI_CURRENT_VALUE;
    public static String GUI_CLICK_TO_TOGGLE;
    public static String GUI_LEFT_CLICK_EDIT;
    public static String GUI_RIGHT_CLICK_RESET;
    public static String GUI_RIGHT_CLICK_DELETE;
    public static String GUI_LOOT_SHOW_IN_FINDER;
    public static String GUI_LOOT_SCORE;
    public static String GUI_LOOT_NICK;
    public static String GUI_LOOT_INSTANT_GAME;
    public static String GUI_LOOT_DISABLE_STATS;
    public static String GUI_LOOT_DISABLE_GAME;
    public static String GUI_ITEM_AMOUNT;
    public static String GUI_ITEM_MODEL_DATA;
    public static String GUI_ITEM_DISPLAY_NAME;
    public static String GUI_ITEM_DURABILITY;
    public static String GUI_ITEM_ENCHANTMENT;
    public static String GUI_ITEM_HEAD64;
    public static String GUI_ITEM_FLAG;
    public static String GUI_ITEM_LORE;
    public static String GUI_ITEM_MATERIAL;
    public static String GUI_ITEM_NBT;
    public static String GUI_ITEM_PREVENT_GRAB;
    public static String GUI_ITEM_PRICE;
    public static String GUI_ITEM_PRICE_BASE;
    public static String GUI_ITEM_PRICE_BONUS;
    public static String GUI_ITEM_RANDOM_DURABILITY;
    public static String GUI_ITEM_SIZE;
    public static String GUI_ITEM_STACKABLE;
    public static String GUI_ITEM_STORED_ENCHANTMENT;
    public static String GUI_ITEM_TAG;
    public static String GUI_ITEM_UNBREAKABLE;
    public static String GUI_DELETE_PROPERTY;
    public static String GUI_NEW_VALUE;
    public static String GUI_CLICK_CONFIRM;
    public static String GUI_INVALID_NUMBER;
    public static String GUI_ILLEGAL_FORMAT;
    public static String GUI_TITLE_AMOUNT;
    public static String GUI_TITLE_MODEL_DATA;
    public static String GUI_TITLE_DISPLAY_NAME;
    public static String GUI_NEW_DISPLAY_NAME;
    public static String GUI_TITLE_CUSTOM_DURABILITY;
    public static String GUI_TITLE_ENCHANTMENT;
    public static String GUI_TITLE_STORED_ENCHANTMENT;
    public static String GUI_SELECT_ONE_ENCHANTMENT;
    public static String GUI_ADD_NEW_ENCHANTMENT;
    public static String GUI_TITLE_ITEM_FLAG;
    public static String GUI_TITLE_LORE;
    public static String GUI_ADD_NEW_LORE;
    public static String GUI_SELECT_ONE_LORE;
    public static String GUI_TITLE_MATERIAL;
    public static String GUI_TITLE_NBT_COMPOUND;
    public static String GUI_TITLE_NBT_LIST;
    public static String GUI_TITLE_NBT_KEY;
    public static String GUI_NBT_INVALID_KEY;
    public static String GUI_RIGHT_CLICK_CANCEL;
    public static String GUI_NBT_ADD_COMPOUND;
    public static String GUI_NBT_ADD_LIST;
    public static String GUI_NBT_ADD_VALUE;
    public static String GUI_NBT_PREVIEW;
    public static String GUI_NBT_BACK_TO_COMPOUND;
    public static String GUI_NBT_SET_VALUE_TITLE;
    public static String GUI_NBT_EDIT_TITLE;
    public static String GUI_NICK_TITLE;
    public static String GUI_NICK_NEW;
    public static String GUI_PRICE_TITLE;
    public static String GUI_PRICE_BASE;
    public static String GUI_PRICE_BONUS;
    public static String GUI_SCORE_TITLE;
    public static String GUI_SIZE_TITLE;
    public static String GUI_SIZE_MIN;
    public static String GUI_SIZE_MAX;
    public static String GUI_SIZE_MAX_NO_LESS;
    public static String GUI_SELECT_FILE;
    public static String GUI_SELECT_ITEM;
    public static String GUI_ADD_NEW_KEY;
    public static String GUI_DUPE_INVALID_KEY;
    public static String GUI_SEARCH;
    public static String GUI_TEMP_NEW_KEY;
    public static String GUI_SET_NEW_KEY;
    public static String GUI_EDIT_KEY;

    public static void load() {
        InputStream inputStream = CustomFishingPlugin.getInstance().getResource("messages/" + CFConfig.language + ".yml");
        if (inputStream != null) {
            try {
                YamlDocument.create(
                        new File(CustomFishingPlugin.getInstance().getDataFolder(), "messages/" + CFConfig.language + ".yml"),
                        inputStream,
                        GeneralSettings.DEFAULT,
                        LoaderSettings
                                .builder()
                                .setAutoUpdate(true)
                                .build(),
                        DumperSettings.DEFAULT,
                        UpdaterSettings
                                .builder()
                                .setVersioning(new BasicVersioning("config-version"))
                                .build()
                );
                inputStream.close();
            } catch (IOException e) {
                LogUtils.warn(e.getMessage());
            }
        }
        loadSettings(CustomFishingPlugin.get().getConfig("messages/" + CFConfig.language + ".yml"));
    }

    private static void loadSettings(YamlConfiguration locale) {
        ConfigurationSection msgSection = locale.getConfigurationSection("messages");
        if (msgSection != null) {
            MSG_Prefix = msgSection.getString("prefix");
            MSG_Reload = msgSection.getString("reload");
            MSG_Competition_Not_Exist = msgSection.getString("competition-not-exist");
            MSG_No_Competition_Ongoing = msgSection.getString("no-competition-ongoing");
            MSG_Stop_Competition = msgSection.getString("stop-competition");
            MSG_End_Competition = msgSection.getString("end-competition");
            MSG_No_Player = msgSection.getString("no-player");
            MSG_No_Score = msgSection.getString("no-score");
            MSG_No_Rank = msgSection.getString("no-rank");
            MSG_Catch_Amount = msgSection.getString("goal-catch-amount");
            MSG_Max_Size = msgSection.getString("goal-max-size");
            MSG_Total_Score = msgSection.getString("goal-total-score");
            MSG_Total_Size = msgSection.getString("goal-total-size");
            MSG_Item_Not_Exists = msgSection.getString("item-not-exist");
            MSG_Get_Item = msgSection.getString("get-item");
            MSG_Give_Item = msgSection.getString("give-item");
            MSG_Never_Played = msgSection.getString("never-played");
            MSG_Unsafe_Modification = msgSection.getString("unsafe-modification");
            MSG_Data_Not_Loaded = msgSection.getString("data-not-loaded");
            MSG_Market_GUI_Open = msgSection.getString("open-market-gui");
            MSG_Fishing_Bag_Open = msgSection.getString("open-fishing-bag");
            MSG_Split_Char = msgSection.getString("split-char");
            MSG_Possible_Loots = msgSection.getString("possible-loots");
            FORMAT_Day = msgSection.getString("format-day");
            FORMAT_Hour = msgSection.getString("format-hour");
            FORMAT_Minute = msgSection.getString("format-minute");
            FORMAT_Second = msgSection.getString("format-second");
        }
        ConfigurationSection guiSection = locale.getConfigurationSection("gui");
        if (guiSection != null) {
            GUI_SEARCH = guiSection.getString("search");
            GUI_EDIT_KEY = guiSection.getString("edit-key");
            GUI_DELETE_PROPERTY = guiSection.getString("delete-property");
            GUI_DUPE_INVALID_KEY = guiSection.getString("dupe-invalid-key");
            GUI_SELECT_ITEM = guiSection.getString("select-item");
            GUI_SELECT_FILE = guiSection.getString("select-file");
            GUI_TEMP_NEW_KEY = guiSection.getString("temp-new-key");
            GUI_SET_NEW_KEY = guiSection.getString("set-new-key");
            GUI_ADD_NEW_KEY = guiSection.getString("page-add-new-key");
            GUI_SCROLL_UP = guiSection.getString("scroll-up");
            GUI_SCROLL_DOWN = guiSection.getString("scroll-down");
            GUI_CANNOT_SCROLL_UP = guiSection.getString("cannot-scroll-up");
            GUI_CANNOT_SCROLL_DOWN = guiSection.getString("cannot-scroll-down");
            GUI_NEXT_PAGE = guiSection.getString("next-page");
            GUI_GOTO_NEXT_PAGE = guiSection.getString("goto-next-page");
            GUI_CANNOT_GOTO_NEXT_PAGE = guiSection.getString("cannot-goto-next-page");
            GUI_PREVIOUS_PAGE = guiSection.getString("previous-page");
            GUI_GOTO_PREVIOUS_PAGE = guiSection.getString("goto-previous-page");
            GUI_CANNOT_GOTO_PREVIOUS_PAGE = guiSection.getString("cannot-goto-previous-page");
            GUI_BACK_TO_PARENT_PAGE = guiSection.getString("back-to-parent-page");
            GUI_BACK_TO_PARENT_FOLDER = guiSection.getString("back-to-parent-folder");
            GUI_CURRENT_VALUE = guiSection.getString("current-value");
            GUI_CLICK_TO_TOGGLE = guiSection.getString("click-to-toggle");
            GUI_LEFT_CLICK_EDIT = guiSection.getString("left-click-edit");
            GUI_RIGHT_CLICK_RESET = guiSection.getString("right-click-reset");
            GUI_RIGHT_CLICK_DELETE = guiSection.getString("right-click-delete");
            GUI_RIGHT_CLICK_CANCEL = guiSection.getString("right-click-cancel");
            GUI_LOOT_SHOW_IN_FINDER = guiSection.getString("loot-show-in-finder");
            GUI_LOOT_SCORE = guiSection.getString("loot-score");
            GUI_LOOT_NICK = guiSection.getString("loot-nick");
            GUI_LOOT_INSTANT_GAME = guiSection.getString("loot-instant-game");
            GUI_LOOT_DISABLE_STATS = guiSection.getString("loot-disable-statistics");
            GUI_LOOT_DISABLE_GAME = guiSection.getString("loot-disable-game");
            GUI_ITEM_AMOUNT = guiSection.getString("item-amount");
            GUI_ITEM_MODEL_DATA = guiSection.getString("item-custom-model-data");
            GUI_ITEM_DISPLAY_NAME = guiSection.getString("item-display-name");
            GUI_ITEM_DURABILITY = guiSection.getString("item-custom-durability");
            GUI_ITEM_ENCHANTMENT = guiSection.getString("item-enchantment");
            GUI_ITEM_HEAD64 = guiSection.getString("item-head64");
            GUI_ITEM_FLAG = guiSection.getString("item-item-flag");
            GUI_ITEM_LORE = guiSection.getString("item-lore");
            GUI_ITEM_MATERIAL = guiSection.getString("item-material");
            GUI_ITEM_NBT = guiSection.getString("item-nbt");
            GUI_ITEM_PREVENT_GRAB = guiSection.getString("item-prevent-grab");
            GUI_ITEM_PRICE = guiSection.getString("item-price");
            GUI_ITEM_PRICE_BASE = guiSection.getString("item-price-base");
            GUI_ITEM_PRICE_BONUS = guiSection.getString("item-price-bonus");
            GUI_ITEM_RANDOM_DURABILITY = guiSection.getString("item-random-durability");
            GUI_ITEM_SIZE = guiSection.getString("item-size");
            GUI_ITEM_STACKABLE = guiSection.getString("item-stackable");
            GUI_ITEM_STORED_ENCHANTMENT = guiSection.getString("item-stored-enchantment");
            GUI_ITEM_TAG = guiSection.getString("item-tag");
            GUI_ITEM_UNBREAKABLE = guiSection.getString("item-unbreakable");
            GUI_NEW_VALUE = guiSection.getString("new-value");
            GUI_CLICK_CONFIRM = guiSection.getString("click-confirm");
            GUI_INVALID_NUMBER = guiSection.getString("invalid-number");
            GUI_ILLEGAL_FORMAT = guiSection.getString("illegal-format");
            GUI_TITLE_AMOUNT = guiSection.getString("page-amount-title");
            GUI_TITLE_MODEL_DATA = guiSection.getString("page-model-data-title");
            GUI_TITLE_DISPLAY_NAME = guiSection.getString("page-display-name-title");
            GUI_NEW_DISPLAY_NAME = guiSection.getString("page-new-display-name");
            GUI_TITLE_CUSTOM_DURABILITY = guiSection.getString("page-custom-durability-title");
            GUI_TITLE_ENCHANTMENT = guiSection.getString("page-enchantment-title");
            GUI_TITLE_STORED_ENCHANTMENT = guiSection.getString("page-stored-enchantment-title");
            GUI_SELECT_ONE_ENCHANTMENT = guiSection.getString("page-select-one-enchantment");
            GUI_ADD_NEW_ENCHANTMENT = guiSection.getString("page-add-new-enchantment");
            GUI_TITLE_ITEM_FLAG = guiSection.getString("page-item-flag-title");
            GUI_TITLE_LORE = guiSection.getString("page-lore-title");
            GUI_ADD_NEW_LORE = guiSection.getString("page-add-new-lore");
            GUI_SELECT_ONE_LORE = guiSection.getString("page-select-one-lore");
            GUI_TITLE_MATERIAL = guiSection.getString("page-material-title");
            GUI_TITLE_NBT_COMPOUND = guiSection.getString("page-nbt-compound-key-title");
            GUI_TITLE_NBT_LIST = guiSection.getString("page-nbt-list-key-title");
            GUI_TITLE_NBT_KEY = guiSection.getString("page-nbt-key-title");
            GUI_NBT_INVALID_KEY = guiSection.getString("page-nbt-invalid-key");
            GUI_NBT_ADD_COMPOUND = guiSection.getString("page-nbt-add-new-compound");
            GUI_NBT_ADD_LIST = guiSection.getString("page-nbt-add-new-list");
            GUI_NBT_ADD_VALUE = guiSection.getString("page-nbt-add-new-value");
            GUI_NBT_PREVIEW = guiSection.getString("page-nbt-preview");
            GUI_NBT_BACK_TO_COMPOUND = guiSection.getString("page-nbt-back-to-compound");
            GUI_NBT_SET_VALUE_TITLE = guiSection.getString("page-nbt-set-value-title");
            GUI_NBT_EDIT_TITLE = guiSection.getString("page-nbt-edit-title");
            GUI_NICK_TITLE = guiSection.getString("page-nick-title");
            GUI_NICK_NEW = guiSection.getString("page-new-nick");
            GUI_PRICE_TITLE = guiSection.getString("page-price-title");
            GUI_PRICE_BASE = guiSection.getString("page-base-price");
            GUI_PRICE_BONUS = guiSection.getString("page-base-bonus");
            GUI_SCORE_TITLE = guiSection.getString("page-score-title");
            GUI_SIZE_TITLE = guiSection.getString("page-size-title");
            GUI_SIZE_MIN = guiSection.getString("page-size-min");
            GUI_SIZE_MAX = guiSection.getString("page-size-max");
            GUI_SIZE_MAX_NO_LESS = guiSection.getString("page-size-max-no-less-min");
        }
    }
}
