package net.momirealms.customfishing.api.mechanic.game;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.bukkit.entity.Player;

public interface GamingPlayer {

    void cancel();

    boolean isSucceeded();

    boolean onRightClick();

    boolean onLeftClick();

    boolean onSwapHand();

    boolean onJump();

    Player getPlayer();

    Effect getEffectReward();

    boolean onChat(String message);
}
