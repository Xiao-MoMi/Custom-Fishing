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

package net.momirealms.customfishing.object;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DynamicText {

    private final Player owner;
    private String originalValue;
    private String latestValue;
    private String[] ownerPlaceholders;

    public DynamicText(Player owner, String rawValue) {
        this.owner = owner;
        analyze(rawValue);
    }

    private void analyze(String value) {
        List<String> placeholdersOwner = new ArrayList<>(CustomFishing.getInstance().getIntegrationManager().getPlaceholderManager().detectPlaceholders(value));
        String origin = value;
        for (String placeholder : placeholdersOwner) {
            origin = origin.replace(placeholder, "%s");
        }
        originalValue = origin;
        ownerPlaceholders = placeholdersOwner.toArray(new String[0]);
        latestValue = originalValue;
        update();
    }

    public String getLatestValue() {
        return latestValue;
    }

    public boolean update() {
        String string = originalValue;
        if (ownerPlaceholders.length != 0) {
            PlaceholderManager placeholderManager = CustomFishing.getInstance().getIntegrationManager().getPlaceholderManager();
            if ("%s".equals(originalValue)) {
                string = placeholderManager.parseSinglePlaceholder(owner, ownerPlaceholders[0]);
            } else {
                Object[] values = new String[ownerPlaceholders.length];
                for (int i = 0; i < ownerPlaceholders.length; i++) {
                    values[i] = placeholderManager.parseSinglePlaceholder(owner, ownerPlaceholders[i]);
                }
                string = String.format(originalValue, values);
            }
        }
        if (!latestValue.equals(string)) {
            latestValue = string;
            return true;
        }
        return false;
    }
}
