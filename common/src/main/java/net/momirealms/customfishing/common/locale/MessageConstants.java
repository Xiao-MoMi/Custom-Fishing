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

package net.momirealms.customfishing.common.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public interface MessageConstants {

    TranslatableComponent.Builder COMPETITION_NO_PLAYER = Component.translatable().key("competition.no_player");;
    TranslatableComponent.Builder COMPETITION_NO_SCORE = Component.translatable().key("competition.no_score");
    TranslatableComponent.Builder COMPETITION_NO_RANK = Component.translatable().key("competition.no_rank");

    TranslatableComponent.Builder GOAL_TOTAL_SIZE = Component.translatable().key("competition.goal.total_size");
    TranslatableComponent.Builder GOAL_CATCH_AMOUNT = Component.translatable().key("competition.goal.catch_amount");
    TranslatableComponent.Builder GOAL_TOTAL_SCORE = Component.translatable().key("competition.goal.total_score");
    TranslatableComponent.Builder GOAL_MAX_SIZE = Component.translatable().key("competition.goal.max_size");
    TranslatableComponent.Builder GOAL_MIN_SIZE = Component.translatable().key("competition.goal.min_size");

    TranslatableComponent.Builder FORMAT_SECOND = Component.translatable().key("format.second");
    TranslatableComponent.Builder FORMAT_MINUTE =  Component.translatable().key("format.minute");
    TranslatableComponent.Builder FORMAT_HOUR =  Component.translatable().key("format.hour");
    TranslatableComponent.Builder FORMAT_DAY =  Component.translatable().key("format.day");

    TranslatableComponent.Builder COMMAND_RELOAD_SUCCESS = Component.translatable().key("command.reload.success");
    TranslatableComponent.Builder COMMAND_ITEM_FAILURE_NOT_EXIST = Component.translatable().key("command.item.failure.not_exist");
    TranslatableComponent.Builder COMMAND_ITEM_GIVE_SUCCESS = Component.translatable().key("command.item.give.success");
    TranslatableComponent.Builder COMMAND_ITEM_GET_SUCCESS = Component.translatable().key("command.item.get.success");
    TranslatableComponent.Builder COMMAND_FISH_FINDER_POSSIBLE_LOOTS = Component.translatable().key("command.fish_finder.possible_loots");
    TranslatableComponent.Builder COMMAND_FISH_FINDER_NO_LOOT = Component.translatable().key("command.fish_finder.no_loot");
    TranslatableComponent.Builder COMMAND_FISH_FINDER_SPLIT_CHAR = Component.translatable().key("command.fish_finder.split_char");
    TranslatableComponent.Builder COMMAND_COMPETITION_FAILURE_NOT_EXIST = Component.translatable().key("command.competition.failure.not_exist");
    TranslatableComponent.Builder COMMAND_COMPETITION_FAILURE_NO_COMPETITION = Component.translatable().key("command.competition.failure.no_competition");
    TranslatableComponent.Builder COMMAND_COMPETITION_START_SUCCESS = Component.translatable().key("command.competition.start.success");
    TranslatableComponent.Builder COMMAND_COMPETITION_STOP_SUCCESS = Component.translatable().key("command.competition.stop.success");
    TranslatableComponent.Builder COMMAND_COMPETITION_END_SUCCESS = Component.translatable().key("command.competition.end.success");
    TranslatableComponent.Builder COMMAND_BAG_EDIT_FAILURE_UNSAFE = Component.translatable().key("command.bag.edit.failure.unsafe");
    TranslatableComponent.Builder COMMAND_BAG_EDIT_FAILURE_NEVER_PLAYED = Component.translatable().key("command.bag.edit.failure.never_played");
    TranslatableComponent.Builder COMMAND_BAG_OPEN_SUCCESS = Component.translatable().key("command.bag.open.success");
    TranslatableComponent.Builder COMMAND_BAG_OPEN_FAILURE_NOT_LOADED = Component.translatable().key("command.bag.open.failure.not_loaded");
    TranslatableComponent.Builder COMMAND_DATA_FAILURE_NOT_LOADED = Component.translatable().key("command.data.failure.not_loaded");
    TranslatableComponent.Builder COMMAND_MARKET_OPEN_SUCCESS = Component.translatable().key("command.market.open.success");
    TranslatableComponent.Builder COMMAND_MARKET_OPEN_FAILURE_NOT_LOADED = Component.translatable().key("command.market.open.failure.not_loaded");
    TranslatableComponent.Builder COMMAND_DATA_UNLOCK_SUCCESS = Component.translatable().key("command.data.unlock.success");
    TranslatableComponent.Builder COMMAND_DATA_IMPORT_FAILURE_NOT_EXISTS = Component.translatable().key("command.data.import.failure.not_exists");
    TranslatableComponent.Builder COMMAND_DATA_IMPORT_FAILURE_PLAYER_ONLINE = Component.translatable().key("command.data.import.failure.player_online");
    TranslatableComponent.Builder COMMAND_DATA_IMPORT_FAILURE_INVALID_FILE = Component.translatable().key("command.data.import.failure.invalid_file");
    TranslatableComponent.Builder COMMAND_DATA_IMPORT_START = Component.translatable().key("command.data.import.start");
    TranslatableComponent.Builder COMMAND_DATA_IMPORT_PROGRESS = Component.translatable().key("command.data.import.progress");
    TranslatableComponent.Builder COMMAND_DATA_IMPORT_SUCCESS = Component.translatable().key("command.data.import.success");
    TranslatableComponent.Builder COMMAND_DATA_EXPORT_FAILURE_PLAYER_ONLINE = Component.translatable().key("command.data.export.failure.player_online");
    TranslatableComponent.Builder COMMAND_DATA_EXPORT_START = Component.translatable().key("command.data.export.start");
    TranslatableComponent.Builder COMMAND_DATA_EXPORT_PROGRESS = Component.translatable().key("command.data.export.progress");
    TranslatableComponent.Builder COMMAND_DATA_EXPORT_SUCCESS = Component.translatable().key("command.data.export.success");
    TranslatableComponent.Builder COMMAND_STATISTICS_FAILURE_NOT_LOADED = Component.translatable().key("command.statistics.failure.not_loaded");
    TranslatableComponent.Builder COMMAND_STATISTICS_FAILURE_UNSUPPORTED = Component.translatable().key("command.statistics.failure.unsupported");
    TranslatableComponent.Builder COMMAND_STATISTICS_MODIFY_SUCCESS = Component.translatable().key("command.statistics.modify.success");
    TranslatableComponent.Builder COMMAND_STATISTICS_RESET_SUCCESS = Component.translatable().key("command.statistics.reset.success");
    TranslatableComponent.Builder COMMAND_STATISTICS_QUERY_AMOUNT = Component.translatable().key("command.statistics.query.amount");
    TranslatableComponent.Builder COMMAND_STATISTICS_QUERY_SIZE = Component.translatable().key("command.statistics.query.size");

//    TranslatableComponent.Builder GUI_SELECT_FILE = Component.translatable().key("gui.select_file");
//    TranslatableComponent.Builder GUI_SELECT_ITEM = Component.translatable().key("gui.select_item");
//    TranslatableComponent.Builder GUI_INVALID_KEY = Component.translatable().key("gui.invalid_key");
//    TranslatableComponent.Builder GUI_NEW_VALUE = Component.translatable().key("gui.new_value");
//    TranslatableComponent.Builder GUI_TEMP_NEW_KEY = Component.translatable().key("gui.temp_new_key");
//    TranslatableComponent.Builder GUI_SET_NEW_KEY = Component.translatable().key("gui.set_new_key");
//    TranslatableComponent.Builder GUI_EDIT_KEY = Component.translatable().key("gui.edit_key");
//    TranslatableComponent.Builder GUI_DELETE_PROPERTY = Component.translatable().key("gui.delete_property");
//    TranslatableComponent.Builder GUI_CLICK_CONFIRM = Component.translatable().key("gui.click_confirm");
//    TranslatableComponent.Builder GUI_INVALID_NUMBER = Component.translatable().key("gui.invalid_number");
//    TranslatableComponent.Builder GUI_ILLEGAL_FORMAT = Component.translatable().key("gui.illegal_format");
//    TranslatableComponent.Builder GUI_SCROLL_UP = Component.translatable().key("gui.scroll_up");
//    TranslatableComponent.Builder GUI_SCROLL_DOWN = Component.translatable().key("gui.scroll_down");
//    TranslatableComponent.Builder GUI_CANNOT_SCROLL_UP = Component.translatable().key("gui.cannot_scroll_up");
//    TranslatableComponent.Builder GUI_CANNOT_SCROLL_DOWN = Component.translatable().key("gui.cannot_scroll_down");
//    TranslatableComponent.Builder GUI_NEXT_PAGE = Component.translatable().key("gui.next_page");
//    TranslatableComponent.Builder GUI_GOTO_NEXT_PAGE = Component.translatable().key("gui.goto_next_page");
//    TranslatableComponent.Builder GUI_CANNOT_GOTO_NEXT_PAGE = Component.translatable().key("gui.cannot_goto_next_page");
//    TranslatableComponent.Builder GUI_PREVIOUS_PAGE = Component.translatable().key("gui.previous_page");
//    TranslatableComponent.Builder GUI_GOTO_PREVIOUS_PAGE = Component.translatable().key("gui.goto_previous_page");
//    TranslatableComponent.Builder GUI_CANNOT_GOTO_PREVIOUS_PAGE = Component.translatable().key("gui.cannot_goto_previous_page");
//    TranslatableComponent.Builder GUI_BACK_TO_PARENT_PAGE = Component.translatable().key("gui.back_to_parent_page");
//    TranslatableComponent.Builder GUI_BACK_TO_PARENT_FOLDER = Component.translatable().key("gui.back_to_parent_folder");
//    TranslatableComponent.Builder GUI_CURRENT_VALUE = Component.translatable().key("gui.current_value");
//    TranslatableComponent.Builder GUI_CLICK_TO_TOGGLE = Component.translatable().key("gui.click_to_toggle");
//    TranslatableComponent.Builder GUI_LEFT_CLICK_EDIT = Component.translatable().key("gui.left_click_edit");
//    TranslatableComponent.Builder GUI_RIGHT_CLICK_RESET = Component.translatable().key("gui.right_click_reset");
//    TranslatableComponent.Builder GUI_RIGHT_CLICK_DELETE = Component.translatable().key("gui.right_click_delete");
//    TranslatableComponent.Builder GUI_RIGHT_CLICK_CANCEL = Component.translatable().key("gui.right_click_cancel");
//    TranslatableComponent.Builder GUI_LOOT_SHOW_IN_FINDER = Component.translatable().key("gui.loot_show_in_finder");
//    TranslatableComponent.Builder GUI_LOOT_SCORE = Component.translatable().key("gui.loot_score");
//    TranslatableComponent.Builder GUI_LOOT_NICK = Component.translatable().key("gui.loot_nick");
//    TranslatableComponent.Builder GUI_LOOT_INSTANT_GAME = Component.translatable().key("gui.loot_instant_game");
//    TranslatableComponent.Builder GUI_LOOT_DISABLE_STATISTICS = Component.translatable().key("gui.loot_disable_statistics");
//    TranslatableComponent.Builder GUI_LOOT_DISABLE_GAME = Component.translatable().key("gui.loot_disable_game");
//    TranslatableComponent.Builder GUI_ITEM_AMOUNT = Component.translatable().key("gui.item_amount");
//    TranslatableComponent.Builder GUI_ITEM_CUSTOM_MODEL_DATA = Component.translatable().key("gui.item_custom_model_data");
//    TranslatableComponent.Builder GUI_ITEM_DISPLAY_NAME = Component.translatable().key("gui.item_display_name");
//    TranslatableComponent.Builder GUI_ITEM_CUSTOM_DURABILITY = Component.translatable().key("gui.item_custom_durability");
//    TranslatableComponent.Builder GUI_ITEM_ENCHANTMENT = Component.translatable().key("gui.item_enchantment");
//    TranslatableComponent.Builder GUI_ITEM_HEAD64 = Component.translatable().key("gui.item_head64");
//    TranslatableComponent.Builder GUI_ITEM_FLAG = Component.translatable().key("gui.item_item_flag");
//    TranslatableComponent.Builder GUI_ITEM_LORE = Component.translatable().key("gui.item_lore");
//    TranslatableComponent.Builder GUI_ITEM_MATERIAL = Component.translatable().key("gui.item_material");
//    TranslatableComponent.Builder GUI_ITEM_NBT = Component.translatable().key("gui.item_nbt");
//    TranslatableComponent.Builder GUI_ITEM_PREVENT_GRAB = Component.translatable().key("gui.item_prevent_grab");
//    TranslatableComponent.Builder GUI_ITEM_PRICE = Component.translatable().key("gui.item_price");
//    TranslatableComponent.Builder GUI_ITEM_PRICE_BASE = Component.translatable().key("gui.item_price_base");
//    TranslatableComponent.Builder GUI_ITEM_PRICE_BONUS = Component.translatable().key("gui.item_price_bonus");
//    TranslatableComponent.Builder GUI_ITEM_RANDOM_DURABILITY = Component.translatable().key("gui.item_random_durability");
//    TranslatableComponent.Builder GUI_ITEM_SIZE = Component.translatable().key("gui.item_size");
//    TranslatableComponent.Builder GUI_ITEM_STACKABLE = Component.translatable().key("gui.item_stackable");
//    TranslatableComponent.Builder GUI_ITEM_STORED_ENCHANTMENT = Component.translatable().key("gui.item_stored_enchantment");
//    TranslatableComponent.Builder GUI_ITEM_TAG = Component.translatable().key("gui.item_tag");
//    TranslatableComponent.Builder GUI_ITEM_UNBREAKABLE = Component.translatable().key("gui.item_unbreakable");
//    TranslatableComponent.Builder GUI_PAGE_AMOUNT_TITLE = Component.translatable().key("gui.page_amount_title");
//    TranslatableComponent.Builder GUI_PAGE_MODEL_DATA_TITLE = Component.translatable().key("gui.page_model_data_title");
//    TranslatableComponent.Builder GUI_PAGE_DISPLAY_NAME_TITLE = Component.translatable().key("gui.page_display_name_title");
//    TranslatableComponent.Builder GUI_PAGE_NEW_DISPLAY_NAME = Component.translatable().key("gui.page_new_display_name");
//    TranslatableComponent.Builder GUI_PAGE_CUSTOM_DURABILITY_TITLE = Component.translatable().key("gui.page_custom_durability_title");
//    TranslatableComponent.Builder GUI_PAGE_STORED_ENCHANTMENT_TITLE = Component.translatable().key("gui.page_stored_enchantment_title");
//    TranslatableComponent.Builder GUI_PAGE_ENCHANTMENT_TITLE = Component.translatable().key("gui.page_enchantment_title");
//    TranslatableComponent.Builder GUI_PAGE_SELECT_ONE_ENCHANTMENT = Component.translatable().key("gui.page_select_one_enchantment");
//    TranslatableComponent.Builder GUI_PAGE_ADD_NEW_ENCHANTMENT = Component.translatable().key("gui.page_add_new_enchantment");
//    TranslatableComponent.Builder GUI_PAGE_ITEM_FLAG_TITLE = Component.translatable().key("gui.page_item_flag_title");
//    TranslatableComponent.Builder GUI_PAGE_LORE_TITLE = Component.translatable().key("gui.page_lore_title");
//    TranslatableComponent.Builder GUI_PAGE_ADD_NEW_LORE = Component.translatable().key("gui.page_add_new_lore");
//    TranslatableComponent.Builder GUI_PAGE_SELECT_ONE_LORE = Component.translatable().key("gui.page_select_one_lore");
//    TranslatableComponent.Builder GUI_PAGE_MATERIAL_TITLE = Component.translatable().key("gui.page_material_title");
//    TranslatableComponent.Builder GUI_PAGE_NBT_COMPOUND_KEY_TITLE = Component.translatable().key("gui.page_nbt_compound_key_title");
//    TranslatableComponent.Builder GUI_PAGE_NBT_LIST_KEY_TITLE = Component.translatable().key("gui.page_nbt_list_key_title");
//    TranslatableComponent.Builder GUI_PAGE_NBT_KEY_TITLE = Component.translatable().key("gui.page_nbt_key_title");
//    TranslatableComponent.Builder GUI_PAGE_NBT_INVALID_KEY = Component.translatable().key("gui.page_nbt_invalid_key");
//    TranslatableComponent.Builder GUI_PAGE_NBT_ADD_NEW_COMPOUND= Component.translatable().key("gui.page_nbt_add_new_compound");
//    TranslatableComponent.Builder GUI_PAGE_NBT_ADD_NEW_LIST = Component.translatable().key("gui.page_nbt_add_new_list");
//    TranslatableComponent.Builder GUI_PAGE_NBT_ADD_NEW_VALUE = Component.translatable().key("gui.page_nbt_add_new_value");
//    TranslatableComponent.Builder GUI_PAGE_ADD_NEW_KEY = Component.translatable().key("gui.page_add_new_key");
//    TranslatableComponent.Builder GUI_PAGE_NBT_PREVIEW = Component.translatable().key("gui.page_nbt_preview");
//    TranslatableComponent.Builder GUI_PAGE_NBT_BACK_TO_COMPOUND = Component.translatable().key("gui.page_nbt_back_to_compound");
//    TranslatableComponent.Builder GUI_PAGE_NBT_SET_VALUE_TITLE = Component.translatable().key("gui.page_nbt_set_value_title");
//    TranslatableComponent.Builder GUI_PAGE_NBT_EDIT_TITLE = Component.translatable().key("gui.page_nbt_edit_title");
//    TranslatableComponent.Builder GUI_PAGE_NICK_TITLE = Component.translatable().key("gui.page_nick_title");
//    TranslatableComponent.Builder GUI_PAGE_NEW_NICK = Component.translatable().key("gui.page_new_nick");
//    TranslatableComponent.Builder GUI_PAGE_PRICE_TITLE = Component.translatable().key("gui.page_price_title");
//    TranslatableComponent.Builder GUI_PAGE_BASE_PRICE = Component.translatable().key("gui.page_base_price");
//    TranslatableComponent.Builder GUI_PAGE_BASE_BONUS = Component.translatable().key("gui.page_base_bonus");
//    TranslatableComponent.Builder GUI_PAGE_SCORE_TITLE = Component.translatable().key("gui.page_score_title");
//    TranslatableComponent.Builder GUI_PAGE_SIZE_TITLE = Component.translatable().key("gui.page_size_title");
//    TranslatableComponent.Builder GUI_PAGE_SIZE_MIN = Component.translatable().key("gui.page_size_min");
//    TranslatableComponent.Builder GUI_PAGE_SIZE_MAX = Component.translatable().key("gui.page_size_max");
//    TranslatableComponent.Builder GUI_PAGE_SIZE_MAX_NO_LESS_MIN = Component.translatable().key("gui.page_size_max_no_less_min");
}
