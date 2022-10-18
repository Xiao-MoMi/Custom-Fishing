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

import net.momirealms.customfishing.object.action.ActionInterface;
import net.momirealms.customfishing.object.fishing.Bonus;
import net.momirealms.customfishing.object.requirements.RequirementInterface;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;

public class Totem {

    private final OriginalModel originalModel;
    private FinalModel finalModel;
    private RequirementInterface[] requirements;
    private final int radius;
    private final Particle particle;
    private final int duration;
    private final Bonus bonus;
    private ActionInterface[] activatorActions;
    private ActionInterface[] nearbyActions;
    private double holoOffset;
    private String[] holoText;
    private PotionEffect[] potionEffects;

    public Totem(OriginalModel originalModel, FinalModel finalModel, int radius, int duration, Particle particle, Bonus bonus) {
        this.originalModel = originalModel;
        this.finalModel = finalModel;
        this.radius = radius;
        this.duration = duration;
        this.particle = particle;
        this.bonus = bonus;
    }

    public RequirementInterface[] getRequirements() {
        return requirements;
    }

    public void setRequirements(RequirementInterface[] requirements) {
        this.requirements = requirements;
    }

    public OriginalModel getOriginalModel() {
        return originalModel;
    }


    public FinalModel getFinalModel() {
        return finalModel;
    }

    public void setFinalModel(FinalModel finalModel) {
        this.finalModel = finalModel;
    }

    public int getRadius() {
        return radius;
    }

    public Particle getParticle() {
        return particle;
    }

    public int getDuration() {
        return duration;
    }

    public Bonus getBonus() {
        return bonus;
    }

    public ActionInterface[] getActivatorActions() {
        return activatorActions;
    }

    public void setActivatorActions(ActionInterface[] activatorActions) {
        this.activatorActions = activatorActions;
    }

    public ActionInterface[] getNearbyActions() {
        return nearbyActions;
    }

    public void setNearbyActions(ActionInterface[] nearbyActions) {
        this.nearbyActions = nearbyActions;
    }

    public double getHoloOffset() {
        return holoOffset;
    }

    public void setHoloOffset(double holoOffset) {
        this.holoOffset = holoOffset;
    }

    public String[] getHoloText() {
        return holoText;
    }

    public void setHoloText(String[] holoText) {
        this.holoText = holoText;
    }

    public PotionEffect[] getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(PotionEffect[] potionEffects) {
        this.potionEffects = potionEffects;
    }
}
