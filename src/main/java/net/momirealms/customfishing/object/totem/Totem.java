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

package net.momirealms.customfishing.object.totem;

import java.util.List;

public class Totem {

    private final OriginalModel originalModel;
    private FinalModel finalModel;
    private List<String> commands;
    private List<String> messages;
    private final boolean cItem;
    private final boolean rItem;

    public Totem(OriginalModel originalModel, boolean rItem, boolean cItem) {
        this.originalModel = originalModel;
        this.rItem = rItem;
        if (rItem) this.cItem = cItem;
        else this.cItem = false;
    }

    public OriginalModel getOriginalModel() {
        return originalModel;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public FinalModel getFinalModel() {
        return finalModel;
    }

    public void setFinalModel(FinalModel finalModel) {
        this.finalModel = finalModel;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public boolean isrItem() {
        return rItem;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean iscItem() {
        return cItem;
    }
}
