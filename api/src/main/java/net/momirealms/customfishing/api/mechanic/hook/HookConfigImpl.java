/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.api.mechanic.hook;

import java.util.List;

public class HookConfigImpl implements HookConfig {

    private final String id;
    private final int maxUsages;
    private final List<String> lore;

    public HookConfigImpl(String id, int maxUsages, List<String> lore) {
        this.id = id;
        this.maxUsages = maxUsages;
        this.lore = lore;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<String> lore() {
        return lore;
    }

    @Override
    public int maxUsages() {
        return maxUsages;
    }

    public static class BuilderImpl implements Builder {
        private String id;
        private int maxUsages;
        private List<String> lore;
        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        @Override
        public Builder maxUsages(int maxUsages) {
            this.maxUsages = maxUsages;
            return this;
        }
        @Override
        public Builder lore(List<String> lore) {
            this.lore = lore;
            return this;
        }
        @Override
        public HookConfig build() {
            return new HookConfigImpl(id, maxUsages, lore);
        }
    }
}
