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

package net.momirealms.customfishing.fishing.totem;

import net.momirealms.customfishing.fishing.Effect;
import net.momirealms.customfishing.fishing.requirements.RequirementInterface;
import net.momirealms.customfishing.object.action.Action;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;

public class TotemConfig {

    private final OriginalModel originalModel;
    private FinalModel finalModel;
    private RequirementInterface[] requirements;
    private final int radius;
    private final Particle particle;
    private final int duration;
    private final Effect effect;
    private Action[] activatorActions;
    private Action[] nearbyActions;
    private double holoOffset;
    private String[] holoText;
    private PotionEffect[] potionEffects;

    public TotemConfig(OriginalModel originalModel, FinalModel finalModel, int radius, int duration, Particle particle, Effect effect) {
        this.originalModel = originalModel;
        this.finalModel = finalModel;
        this.radius = radius;
        this.duration = duration;
        this.particle = particle;
        this.effect = effect;
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

    public Effect getBonus() {
        return effect;
    }

    public Action[] getActivatorActions() {
        return activatorActions;
    }

    public void setActivatorActions(Action[] activatorActions) {
        this.activatorActions = activatorActions;
    }

    public Action[] getNearbyActions() {
        return nearbyActions;
    }

    public void setNearbyActions(Action[] nearbyActions) {
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
