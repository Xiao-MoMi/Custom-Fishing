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

package net.momirealms.customfishing.listener;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.hook.*;
import net.momirealms.customfishing.object.*;
import net.momirealms.customfishing.object.Difficulty;
import net.momirealms.customfishing.object.action.ActionB;
import net.momirealms.customfishing.object.loot.DroppedItem;
import net.momirealms.customfishing.object.loot.Loot;
import net.momirealms.customfishing.object.loot.Mob;
import net.momirealms.customfishing.utils.AdventureUtil;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.requirements.FishingCondition;
import net.momirealms.customfishing.requirements.Requirement;
import net.momirealms.customfishing.titlebar.Timer;
import net.momirealms.customfishing.utils.ItemStackUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishListener implements Listener {

    private final HashMap<Player, Long> coolDown = new HashMap<>();
    private final HashMap<Player, Loot> nextLoot = new HashMap<>();
    private final HashMap<Player, Modifier> modifiers = new HashMap<>();
    private final HashMap<Player, FishHook> hooks = new HashMap<>();
    private final HashMap<Player, VanillaLoot> vanilla = new HashMap<>();
    public static ConcurrentHashMap<Player, FishingPlayer> fishingPlayers = new ConcurrentHashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent event){

        PlayerFishEvent.State state = event.getState();
        Player player = event.getPlayer();

        if (state.equals(PlayerFishEvent.State.FISHING)){

            long time = System.currentTimeMillis();
            if (time - (coolDown.getOrDefault(player, time - 2000)) < 2000) {
                return;
            }
            coolDown.put(player, time);

            hooks.put(player, event.getHook());

            Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.instance, ()->{

                PlayerInventory inventory = player.getInventory();

                boolean noSpecialRod = true;
                boolean noRod = true;
                double timeModifier = 1;
                double doubleLoot = 0;
                double scoreModifier = 1;
                int difficultyModifier = 0;

                HashMap<String, Integer> pm = new HashMap<>();
                HashMap<String, Double> mq = new HashMap<>();

                ItemStack mainHandItem = inventory.getItemInMainHand();

                Material material1 = mainHandItem.getType();
                if (material1 != Material.AIR){
                    if (material1 == Material.FISHING_ROD) {
                        noRod = false;
                        Map<Enchantment, Integer> enchantments = mainHandItem.getEnchantments();
                        Object[] enchantmentsArray = enchantments.keySet().toArray();
                        for (Object o : enchantmentsArray) {
                            Enchantment enchantment = (Enchantment) o;
                            HashMap<Integer, Bonus> enchantMap = ConfigReader.ENCHANTS.get(enchantment.getKey().toString());
                            if (enchantMap != null) {
                                Bonus enchantBonus = enchantMap.get(enchantments.get(enchantment));
                                if (enchantBonus != null) {
                                    HashMap<String, Integer> weightPM = enchantBonus.getWeightPM();
                                    if (weightPM != null){
                                        Object[] bonus = weightPM.keySet().toArray();
                                        for (Object value : bonus) {
                                            String group = (String) value;
                                            pm.put(group, Optional.ofNullable(pm.get(group)).orElse(0) + weightPM.get(group));
                                        }
                                    }
                                    HashMap<String, Integer> weightMQ = enchantBonus.getWeightPM();
                                    if (weightMQ != null){
                                        Object[] bonus = weightMQ.keySet().toArray();
                                        for (Object value : bonus) {
                                            String group = (String) value;
                                            mq.put(group, Optional.ofNullable(mq.get(group)).orElse(0d) + weightMQ.get(group));
                                        }
                                    }
                                    if (enchantBonus.getTime() != 0) timeModifier *= enchantBonus.getTime();
                                    if (enchantBonus.getDoubleLoot() != 0) doubleLoot += enchantBonus.getDoubleLoot();
                                    if (enchantBonus.getDifficulty() != 0) difficultyModifier += enchantBonus.getDifficulty();
                                    if (enchantBonus.getScore() != 0) scoreModifier *= enchantBonus.getScore();
                                }
                            }
                        }
                    }
                    NBTItem nbtItem = new NBTItem(inventory.getItemInMainHand());
                    NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                    if (nbtCompound != null){
                        if (nbtCompound.getString("type").equals("rod")) {
                            String key = nbtCompound.getString("id");
                            Bonus rod = ConfigReader.ROD.get(key);
                            if (rod != null){
                                HashMap<String, Integer> weightPM = rod.getWeightPM();
                                if (weightPM != null){
                                    Object[] bonus = weightPM.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        pm.put(group, Optional.ofNullable(pm.get(group)).orElse(0) + weightPM.get(group));
                                    }
                                }
                                HashMap<String, Integer> weightMQ = rod.getWeightPM();
                                if (weightMQ != null){
                                    Object[] bonus = weightMQ.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        mq.put(group, Optional.ofNullable(mq.get(group)).orElse(0d) + weightMQ.get(group));
                                    }
                                }
                                if (rod.getTime() != 0) timeModifier *= rod.getTime();
                                if (rod.getDoubleLoot() != 0) doubleLoot += rod.getDoubleLoot();
                                if (rod.getDifficulty() != 0) difficultyModifier += rod.getDifficulty();
                                if (rod.getScore() != 0) scoreModifier *= rod.getScore();
                                noSpecialRod = false;
                            }
                        }
                        else if (nbtCompound.getString("type").equals("bait")){
                            String key = nbtCompound.getString("id");
                            Bonus bait = ConfigReader.BAIT.get(key);
                            if (bait != null){
                                HashMap<String, Integer> weightPM = bait.getWeightPM();
                                if (weightPM != null){
                                    Object[] bonus = weightPM.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        pm.put(group, Optional.ofNullable(pm.get(group)).orElse(0) + weightPM.get(group));
                                    }
                                }
                                HashMap<String, Integer> weightMQ = bait.getWeightPM();
                                if (weightMQ != null){
                                    Object[] bonus = weightMQ.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        mq.put(group, Optional.ofNullable(mq.get(group)).orElse(0d) + weightMQ.get(group));
                                    }
                                }
                                if (bait.getTime() != 0) timeModifier *= bait.getTime();
                                if (bait.getDoubleLoot() != 0) doubleLoot += bait.getDoubleLoot();
                                if (bait.getDifficulty() != 0) difficultyModifier += bait.getDifficulty();
                                if (bait.getScore() != 0) scoreModifier *= bait.getScore();
                                mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                            }
                        }
                    }
                }

                ItemStack offHandItem = inventory.getItemInOffHand();
                Material material2 = offHandItem.getType();
                if (material2 != Material.AIR){
                    if (noRod && material2 == Material.FISHING_ROD) {
                        Map<Enchantment, Integer> enchantments = mainHandItem.getEnchantments();
                        Object[] enchantmentsArray = enchantments.keySet().toArray();
                        for (Object o : enchantmentsArray) {
                            Enchantment enchantment = (Enchantment) o;
                            HashMap<Integer, Bonus> enchantMap = ConfigReader.ENCHANTS.get(enchantment.getKey().toString());
                            if (enchantMap != null) {
                                Bonus enchantBonus = enchantMap.get(enchantments.get(enchantment));
                                if (enchantBonus != null) {
                                    HashMap<String, Integer> weightPM = enchantBonus.getWeightPM();
                                    if (weightPM != null){
                                        Object[] bonus = weightPM.keySet().toArray();
                                        for (Object value : bonus) {
                                            String group = (String) value;
                                            pm.put(group, Optional.ofNullable(pm.get(group)).orElse(0) + weightPM.get(group));
                                        }
                                    }
                                    HashMap<String, Integer> weightMQ = enchantBonus.getWeightPM();
                                    if (weightMQ != null){
                                        Object[] bonus = weightMQ.keySet().toArray();
                                        for (Object value : bonus) {
                                            String group = (String) value;
                                            mq.put(group, Optional.ofNullable(mq.get(group)).orElse(0d) + weightMQ.get(group));
                                        }
                                    }
                                    if (enchantBonus.getTime() != 0) timeModifier *= enchantBonus.getTime();
                                    if (enchantBonus.getDoubleLoot() != 0) doubleLoot += enchantBonus.getDoubleLoot();
                                    if (enchantBonus.getDifficulty() != 0) difficultyModifier += enchantBonus.getDifficulty();
                                    if (enchantBonus.getScore() != 0) scoreModifier *= enchantBonus.getScore();
                                }
                            }
                        }
                    }
                    NBTItem offHand = new NBTItem(inventory.getItemInOffHand());
                    NBTCompound offHandCompound = offHand.getCompound("CustomFishing");
                    if (offHandCompound != null){
                        if (offHandCompound.getString("type").equals("bait")) {
                            String key = offHandCompound.getString("id");
                            Bonus bait = ConfigReader.BAIT.get(key);
                            if (bait != null){
                                HashMap<String, Integer> weightPM = bait.getWeightPM();
                                if (weightPM != null){
                                    Object[] bonus = weightPM.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        pm.put(group, Optional.ofNullable(pm.get(group)).orElse(0) + weightPM.get(group));
                                    }
                                }
                                HashMap<String, Integer> weightMQ = bait.getWeightPM();
                                if (weightMQ != null){
                                    Object[] bonus = weightMQ.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        mq.put(group, Optional.ofNullable(mq.get(group)).orElse(0d) + weightMQ.get(group));
                                    }
                                }
                                if (bait.getTime() != 0) timeModifier *= bait.getTime();
                                if (bait.getDoubleLoot() != 0) doubleLoot += bait.getDoubleLoot();
                                if (bait.getDifficulty() != 0) difficultyModifier += bait.getDifficulty();
                                if (bait.getScore() != 0) scoreModifier *= bait.getScore();
                                offHandItem.setAmount(offHandItem.getAmount() - 1);
                            }
                        }
                        else if (noSpecialRod && offHandCompound.getString("type").equals("rod")){
                            String key = offHandCompound.getString("id");
                            Bonus rod = ConfigReader.ROD.get(key);
                            if (rod != null){
                                HashMap<String, Integer> weightPM = rod.getWeightPM();
                                if (weightPM != null){
                                    Object[] bonus = weightPM.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        pm.put(group, Optional.ofNullable(pm.get(group)).orElse(0) + weightPM.get(group));
                                    }
                                }
                                HashMap<String, Integer> weightMQ = rod.getWeightPM();
                                if (weightMQ != null){
                                    Object[] bonus = weightMQ.keySet().toArray();
                                    for (Object value : bonus) {
                                        String group = (String) value;
                                        mq.put(group, Optional.ofNullable(mq.get(group)).orElse(0d) + weightMQ.get(group));
                                    }
                                }
                                if (rod.getTime() != 0) timeModifier *= rod.getTime();
                                if (rod.getDoubleLoot() != 0) doubleLoot += rod.getDoubleLoot();
                                if (rod.getDifficulty() != 0) difficultyModifier += rod.getDifficulty();
                                if (rod.getScore() != 0) scoreModifier *= rod.getScore();
                                noSpecialRod = false;
                            }
                        }
                    }
                }

                if (ConfigReader.Config.needSpecialRod && noSpecialRod){
                    if (!ConfigReader.Config.vanillaLoot)
                        AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.noRod);
                    nextLoot.put(player, null);
                    return;
                }

                FishHook hook = event.getHook();
                hook.setMaxWaitTime((int) (timeModifier * hook.getMaxWaitTime()));
                hook.setMinWaitTime((int) (timeModifier * hook.getMinWaitTime()));

                List<Loot> possibleLoots = getPossibleLootList(new FishingCondition(hook.getLocation(), player));
                List<Loot> availableLoots = new ArrayList<>();

                if (possibleLoots.size() == 0){
                    nextLoot.put(player, null);
                    return;
                }

                Modifier modifier = new Modifier();
                modifier.setDifficulty(difficultyModifier);
                modifier.setScore(scoreModifier);
                modifier.setWillDouble(doubleLoot > Math.random());
                modifiers.put(player, modifier);


                double[] weights = new double[possibleLoots.size()];
                int index = 0;
                for (Loot loot : possibleLoots){
                    double weight = loot.getWeight();
                    String group = loot.getGroup();
                    if (group != null){
                        if (pm.get(group) != null){
                            weight += pm.get(group);
                        }
                        if (mq.get(group) != null){
                            weight *= mq.get(group);
                        }
                    }
                    if (weight <= 0) continue;
                    availableLoots.add(loot);
                    weights[index++] = weight;
                }

                double total = Arrays.stream(weights).sum();
                double[] weightRatios = new double[index];
                for (int i = 0; i < index; i++){
                    weightRatios[i] = weights[i]/total;
                }

                double[] weightRange = new double[index];
                double startPos = 0;
                for (int i = 0; i < index; i++) {
                    weightRange[i] = startPos + weightRatios[i];
                    startPos += weightRatios[i];
                }

                double random = Math.random();
                int pos = Arrays.binarySearch(weightRange, random);

                if (pos < 0) {
                    pos = -pos - 1;
                } else {
                    nextLoot.put(player, availableLoots.get(pos));
                    return;
                }
                if (pos < weightRange.length && random < weightRange[pos]) {
                    nextLoot.put(player, availableLoots.get(pos));
                    return;
                }
                nextLoot.put(player, null);
            });
        }

        else if (state.equals(PlayerFishEvent.State.BITE)){

            if (ConfigReader.Config.doubleRealIn) return;

            if (fishingPlayers.get(player) != null) return;

            Loot loot = nextLoot.get(player);

            if (loot == null) return;

            String layout;
            if (loot.getLayout() != null){
                layout = loot.getLayout().get((int) (loot.getLayout().size() * Math.random()));
            }else {
                Object[] values = ConfigReader.LAYOUT.keySet().toArray();
                layout = (String) values[new Random().nextInt(values.length)];
            }

            int difficulty = loot.getDifficulty().getSpeed();
            difficulty += Objects.requireNonNullElse(modifiers.get(player).getDifficulty(), 0);;
            if (difficulty < 1){
                difficulty = 1;
            }

            Difficulty difficult = new Difficulty(loot.getDifficulty().getTimer(), difficulty);
            fishingPlayers.put(player,
                    new FishingPlayer(System.currentTimeMillis() + loot.getTime(),
                            new Timer(player, difficult, layout)
                    )
            );

            for (ActionB action : loot.getHookActions()){
                action.doOn(player);
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, loot.getTime()/50,3));

        }

        //收杆拉鱼
        else if (state.equals(PlayerFishEvent.State.CAUGHT_FISH)) {

            //是否需要两次拉杆
            if (ConfigReader.Config.doubleRealIn) {

                FishingPlayer fishingPlayer = fishingPlayers.remove(player);
                if (fishingPlayer == null){
                    Entity entity = event.getCaught();
                    if (entity instanceof Item item){
                        //是否有原版战利品
                        if (ConfigReader.Config.vanillaLoot) {
                            //不是原版战利品
                            if (ConfigReader.Config.vanillaRatio < Math.random()) {
                                event.setCancelled(true);
                                vanilla.remove(player);
                                showPlayerBar(player);
                            }
                            //是原版战利品
                            else {
                                //需要走力度条流程
                                if (ConfigReader.Config.showBar){
                                    event.setCancelled(true);
                                    vanilla.put(player, new VanillaLoot(item.getItemStack(), event.getExpToDrop()));
                                    showPlayerBar(player);
                                }
                            }
                        }
                        //如果不许有原版战利品则清除
                        else {
                            if (nextLoot.get(player) == null){
                                item.remove();
                                event.setExpToDrop(0);
                                AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.noLoot);
                            }
                            else {
                                event.setCancelled(true);
                                showPlayerBar(player);
                            }
                        }
                    }
                }
                else {
                    Entity entity = event.getCaught();
                    if (entity instanceof Item item){
                        item.remove();
                        event.setExpToDrop(0);
                    }
                    Loot loot = nextLoot.remove(player);
                    VanillaLoot vanillaLoot = vanilla.remove(player);
                    Timer timer = fishingPlayer.getTimer();
                    Layout layout = ConfigReader.LAYOUT.get(timer.getLayout());
                    int last = (timer.getTimerTask().getProgress())/layout.getRange();

                    player.removePotionEffect(PotionEffectType.SLOW);
                    if (ConfigReader.Config.needOpenWater && !event.getHook().isInOpenWater()){
                        AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.notOpenWater);
                        return;
                    }

                    if (Math.random() < layout.getSuccessRate()[last]){
                        if (ConfigReader.Config.loseDurability)
                            loseDurability(player);
                        Location location = event.getHook().getLocation();
                        if (loot instanceof Mob mob){
                            MythicMobsUtil.summonMM(player.getLocation(), location, mob);
                            for (ActionB action : loot.getSuccessActions())
                                action.doOn(player);
                            if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()) {
                                float score = (float) (loot.getScore() * modifiers.get(player).getScore());
                                CompetitionSchedule.competition.refreshRanking(player.getName(), score);
                                BossBarManager.joinCompetition(player);
                            }
                        }
                        else if (loot instanceof DroppedItem droppedItem){
                            ItemStack itemStack;
                            if (vanillaLoot != null) {
                                itemStack = vanillaLoot.getItemStack();
                                player.giveExp(vanillaLoot.getXp(), true);
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
                                Entity item = location.getWorld().dropItem(location, itemStack);
                                Vector vector = player.getLocation().subtract(location).toVector().multiply(0.1);
                                vector = vector.setY((vector.getY()+0.2)*1.2);
                                item.setVelocity(vector);
                                if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()) {
                                    CompetitionSchedule.competition.refreshRanking(player.getName(), 0);
                                    BossBarManager.joinCompetition(player);
                                }
                                if (modifiers.get(player).isWillDouble()) {
                                    Entity item2 = location.getWorld().dropItem(location, itemStack);
                                    item2.setVelocity(vector);
                                }
                                String title = ConfigReader.Title.success_title.get((int) (ConfigReader.Title.success_title.size()*Math.random()));
                                Component titleComponent = getTitleComponent(itemStack, title);
                                String subTitle = ConfigReader.Title.success_subtitle.get((int) (ConfigReader.Title.success_subtitle.size()*Math.random()));
                                Component subtitleComponent = getTitleComponent(itemStack, subTitle);

                                AdventureUtil.playerTitle(
                                        player,
                                        titleComponent,
                                        subtitleComponent,
                                        ConfigReader.Title.success_in,
                                        ConfigReader.Title.success_stay,
                                        ConfigReader.Title.success_out
                                );
                            }
                            else {
                                if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()) {
                                    float score = (float) (loot.getScore() * modifiers.get(player).getScore());
                                    CompetitionSchedule.competition.refreshRanking(player.getName(), score);
                                    BossBarManager.joinCompetition(player);
                                }
                                for (ActionB action : loot.getSuccessActions())
                                    action.doOn(player);
                                dropLoot(player, location, droppedItem);
                                AdventureUtil.playerTitle(
                                        player,
                                        ConfigReader.Title.success_title.get((int) (ConfigReader.Title.success_title.size()*Math.random()))
                                                .replace("{loot}",loot.getNick())
                                                .replace("{player}", player.getName()),
                                        ConfigReader.Title.success_subtitle.get((int) (ConfigReader.Title.success_subtitle.size()*Math.random()))
                                                .replace("{loot}",loot.getNick())
                                                .replace("{player}", player.getName()),
                                        ConfigReader.Title.success_in,
                                        ConfigReader.Title.success_stay,
                                        ConfigReader.Title.success_out
                                );
                            }
                        }
                    }
                    else if (vanillaLoot == null) {
                        fail(player, loot);
                    }
                }
            }
            //不需要两次拉杆
            //除非设置否则肯定不会有原版掉落物
            else {

                Entity entity = event.getCaught();
                if (entity instanceof Item item){

                    //如果玩家正在钓鱼
                    //那么拉杆的时候可能也会遇到上钩点，进行正常收杆判断
                    FishingPlayer fishingPlayer = fishingPlayers.remove(player);
                    if (fishingPlayer != null){

                        item.remove();
                        event.setExpToDrop(0);

                        Loot loot = nextLoot.get(player);
                        Timer timer = fishingPlayer.getTimer();
                        Layout layout = ConfigReader.LAYOUT.get(timer.getLayout());
                        int last = (timer.getTimerTask().getProgress())/layout.getRange();
                        player.removePotionEffect(PotionEffectType.SLOW);

                        if (ConfigReader.Config.needOpenWater && !event.getHook().isInOpenWater()){
                            AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.notOpenWater);
                            return;
                        }

                        //捕鱼成功
                        if (Math.random() < layout.getSuccessRate()[last]) {
                            Location location = event.getHook().getLocation();
                            if (loot instanceof Mob mob){
                                MythicMobsUtil.summonMM(player.getLocation(), location, mob);
                            }
                            else if (loot instanceof DroppedItem droppedItem){
                                dropLoot(player, location, droppedItem);
                            }
                            for (ActionB action : loot.getSuccessActions())
                                action.doOn(player);
                            if (ConfigReader.Config.loseDurability)
                                loseDurability(player);
                            if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()){
                                float score = (float) (loot.getScore() * modifiers.get(player).getScore());
                                CompetitionSchedule.competition.refreshRanking(player.getName(), score);
                                BossBarManager.joinCompetition(player);
                            }
                            AdventureUtil.playerTitle(
                                    player,
                                    ConfigReader.Title.success_title.get((int) (ConfigReader.Title.success_title.size()*Math.random()))
                                            .replace("{loot}",loot.getNick())
                                            .replace("{player}", player.getName()),
                                    ConfigReader.Title.success_subtitle.get((int) (ConfigReader.Title.success_subtitle.size()*Math.random()))
                                            .replace("{loot}",loot.getNick())
                                            .replace("{player}", player.getName()),
                                    ConfigReader.Title.success_in,
                                    ConfigReader.Title.success_stay,
                                    ConfigReader.Title.success_out
                            );
                        }
                        //捕鱼失败
                        else {
                            fail(player, loot);
                        }
                    }
                    else {
                        if (!ConfigReader.Config.vanillaLoot) {
                            item.remove();
                            event.setExpToDrop(0);
                        }
                    }
                }
            }
        }

        //普通收杆
        //对于在Vanilla HashMap中有值的，说明是有原版战利品，否则全部是插件战利品
        else if (state.equals(PlayerFishEvent.State.REEL_IN)){

            FishingPlayer fishingPlayer = fishingPlayers.remove(player);
            //首先得是钓鱼中的玩家
            if (fishingPlayer != null){

                Loot loot = nextLoot.remove(player);
                VanillaLoot vanillaLoot = vanilla.remove(player);
                Timer timer = fishingPlayer.getTimer();
                Layout layout = ConfigReader.LAYOUT.get(timer.getLayout());
                int last = (timer.getTimerTask().getProgress())/layout.getRange();

                player.removePotionEffect(PotionEffectType.SLOW);
                if (ConfigReader.Config.needOpenWater && !event.getHook().isInOpenWater()){
                    AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.notOpenWater);
                    return;
                }

                if (Math.random() < layout.getSuccessRate()[last]){
                    if (ConfigReader.Config.loseDurability)
                        loseDurability(player);
                    Location location = event.getHook().getLocation();
                    if (loot instanceof Mob mob){
                        MythicMobsUtil.summonMM(player.getLocation(), location, mob);
                        for (ActionB action : loot.getSuccessActions())
                            action.doOn(player);
                        if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()) {
                            float score = (float) (loot.getScore() * modifiers.get(player).getScore());
                            CompetitionSchedule.competition.refreshRanking(player.getName(), score);
                            BossBarManager.joinCompetition(player);
                        }
                    }
                    else if (loot instanceof DroppedItem droppedItem){
                        ItemStack itemStack;

                        if (vanillaLoot != null) {
                            itemStack = vanillaLoot.getItemStack();
                            player.giveExp(vanillaLoot.getXp(), true);
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
                            Entity item = location.getWorld().dropItem(location, itemStack);
                            Vector vector = player.getLocation().subtract(location).toVector().multiply(0.1);
                            vector = vector.setY((vector.getY()+0.2)*1.2);
                            item.setVelocity(vector);
                            if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()) {
                                CompetitionSchedule.competition.refreshRanking(player.getName(), 0);
                                BossBarManager.joinCompetition(player);
                            }
                            if (modifiers.get(player).isWillDouble()) {
                                Entity item2 = location.getWorld().dropItem(location, itemStack);
                                item2.setVelocity(vector);
                            }

                            String title = ConfigReader.Title.success_title.get((int) (ConfigReader.Title.success_title.size()*Math.random()));
                            Component titleComponent = getTitleComponent(itemStack, title);
                            String subTitle = ConfigReader.Title.success_subtitle.get((int) (ConfigReader.Title.success_subtitle.size()*Math.random()));
                            Component subtitleComponent = getTitleComponent(itemStack, subTitle);

                            AdventureUtil.playerTitle(
                                    player,
                                    titleComponent,
                                    subtitleComponent,
                                    ConfigReader.Title.success_in,
                                    ConfigReader.Title.success_stay,
                                    ConfigReader.Title.success_out
                            );
                        }
                        else {
                            if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()) {
                                float score = (float) (loot.getScore() * modifiers.get(player).getScore());
                                CompetitionSchedule.competition.refreshRanking(player.getName(), score);
                                BossBarManager.joinCompetition(player);
                            }
                            for (ActionB action : loot.getSuccessActions())
                                action.doOn(player);
                            dropLoot(player, location, droppedItem);
                            AdventureUtil.playerTitle(
                                    player,
                                    ConfigReader.Title.success_title.get((int) (ConfigReader.Title.success_title.size()*Math.random()))
                                            .replace("{loot}",loot.getNick())
                                            .replace("{player}", player.getName()),
                                    ConfigReader.Title.success_subtitle.get((int) (ConfigReader.Title.success_subtitle.size()*Math.random()))
                                            .replace("{loot}",loot.getNick())
                                            .replace("{player}", player.getName()),
                                    ConfigReader.Title.success_in,
                                    ConfigReader.Title.success_stay,
                                    ConfigReader.Title.success_out
                            );
                        }
                    }
                }
                else if (vanillaLoot == null) {
                    fail(player, loot);
                }
            }
            else {
                //钓上来的是物品
                if (event.getCaught() instanceof Item item) {
                    //是否允许原版掉落物
                    if (ConfigReader.Config.vanillaLoot) {
                        if (ConfigReader.Config.showBar){
                            item.remove();
                            event.setExpToDrop(0);
                            //event.setCancelled(true);
                            vanilla.put(player, new VanillaLoot(item.getItemStack(), event.getExpToDrop()));
                            showPlayerBar(player);
                        }
                        else {
                            //啥也不干
                        }
                    }
                    //不允许原版掉落物
                    else {
                        item.remove();
                        event.setExpToDrop(0);
                        //event.setCancelled(true);
                    }
                }
            }
        }
        else if (state.equals(PlayerFishEvent.State.CAUGHT_ENTITY)){
            //理论是不存在实体的
            //说明在钓鱼的时候可能鱼钩勾上了鱿鱼之类的生物
            //直接按照失败处理
            if (fishingPlayers.remove(player) != null && event.getCaught() != null){
                AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.hookOther);
            }
        }
    }

    @NotNull
    private Component getTitleComponent(ItemStack itemStack, String text) {
        Component subtitleComponent;
        if (text.contains("{loot}")){
            text = text.replace("{loot}","|");
            if (text.startsWith("|")){
                subtitleComponent = itemStack.displayName().append(MiniMessage.miniMessage().deserialize(text.substring(1)));
            }
            else if (text.endsWith("|")){
                subtitleComponent = MiniMessage.miniMessage().deserialize(text.substring(0,text.length()-1)).append(itemStack.displayName());
            }
            else {
                String[] titleSplit = StringUtils.split(text, "|");
                subtitleComponent = MiniMessage.miniMessage().deserialize(titleSplit[0]).append(itemStack.displayName()).append(MiniMessage.miniMessage().deserialize(titleSplit[1]));
            }
        }
        else {
            subtitleComponent = MiniMessage.miniMessage().deserialize(text);
        }
        return subtitleComponent;
    }

    private void dropLoot(Player player, Location location, DroppedItem droppedItem) {
        ItemStack itemStack;
        switch (droppedItem.getType()){
            case "ia" -> itemStack = ItemsAdderItem.getItemStack(droppedItem.getId()).clone();
            case "oraxen" -> itemStack = OraxenItem.getItemStack(droppedItem.getId()).clone();
            case "mm" -> itemStack = MythicItems.getItemStack(droppedItem.getId()).clone();
            case "mmoitems" -> itemStack = MMOItemsHook.getItemStack(droppedItem.getId()).clone();
            default -> itemStack = ConfigReader.LootItem.get(droppedItem.getKey()).clone();
        }

        if (itemStack.getType() != Material.AIR) {
            if (droppedItem.getRandomEnchants() != null)
                ItemStackUtil.addRandomEnchants(itemStack, droppedItem.getRandomEnchants());
            if (droppedItem.isRandomDurability())
                ItemStackUtil.addRandomDamage(itemStack);
            if (ConfigReader.Config.preventPick)
                ItemStackUtil.addOwner(itemStack, player.getName());
            Entity item = location.getWorld().dropItem(location, itemStack);
            Vector vector = player.getLocation().subtract(location).toVector().multiply(0.1);
            vector = vector.setY((vector.getY()+0.2)*1.2);
            item.setVelocity(vector);
            if (modifiers.get(player).isWillDouble()){
                Entity item2 = location.getWorld().dropItem(location, itemStack);
                item2.setVelocity(vector);
            }
        }
    }

    private void loseDurability(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        PlayerInventory inventory = player.getInventory();
        ItemStack mainHand = inventory.getItemInMainHand();
        if (mainHand.getType() == Material.FISHING_ROD){
            Damageable damageable = (Damageable) mainHand.getItemMeta();
            if (damageable.isUnbreakable()) return;
            Enchantment enchantment = Enchantment.DURABILITY;
            if (Math.random() < (1/(double) (damageable.getEnchantLevel(enchantment) + 1))){
                damageable.setDamage(damageable.getDamage() + 1);
                Bukkit.getScheduler().runTaskLater(CustomFishing.instance, ()->{
                    mainHand.setItemMeta(damageable);
                },1);
            }
        }
        else {
            ItemStack offHand = inventory.getItemInOffHand();
            if (offHand.getType() == Material.FISHING_ROD){
                Damageable damageable = (Damageable) offHand.getItemMeta();
                if (damageable.isUnbreakable()) return;
                Enchantment enchantment = Enchantment.DURABILITY;
                if (Math.random() < (1/(double) (damageable.getEnchantLevel(enchantment) + 1))){
                    damageable.setDamage(damageable.getDamage() + 1);
                    Bukkit.getScheduler().runTaskLater(CustomFishing.instance, ()->{
                        offHand.setItemMeta(damageable);
                    },1);
                }
            }
        }
    }

    private void fail(Player player, Loot loot) {
        fishingPlayers.remove(player);
        for (ActionB action : loot.getFailureActions())
            action.doOn(player);
        AdventureUtil.playerTitle(player, ConfigReader.Title.failure_title.get((int) (ConfigReader.Title.failure_title.size()*Math.random())), ConfigReader.Title.failure_subtitle.get((int) (ConfigReader.Title.failure_subtitle.size()*Math.random())), ConfigReader.Title.failure_in, ConfigReader.Title.failure_stay, ConfigReader.Title.failure_out);
    }

    @EventHandler
    public void onQUit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        player.removePotionEffect(PotionEffectType.SLOW);
        if (hooks.get(player) != null){
            hooks.get(player).remove();
        }
        hooks.remove(player);
        coolDown.remove(player);
        nextLoot.remove(player);
        modifiers.remove(player);
        fishingPlayers.remove(player);
        vanilla.remove(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getCompound("CustomFishing") == null) return;
        if (nbtItem.getCompound("CustomFishing").getString("type").equals("util") && nbtItem.getCompound("CustomFishing").getString("id").equals("fishfinder")){
            Player player = event.getPlayer();
            //设置冷却时间
            long time = System.currentTimeMillis();
            if (time - (coolDown.getOrDefault(player, time - ConfigReader.Config.fishFinderCoolDown)) < ConfigReader.Config.fishFinderCoolDown) {
                AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.coolDown);
                return;
            }
            coolDown.put(player, time);
            //获取玩家位置处可能的Loot实例列表
            List<Loot> possibleLoots = getFinder(new FishingCondition(player.getLocation(), player));
            if (possibleLoots.size() == 0){
                AdventureUtil.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.noLoot);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder(ConfigReader.Message.prefix + ConfigReader.Message.possibleLoots);
            possibleLoots.forEach(loot -> stringBuilder.append(loot.getNick()).append(ConfigReader.Message.splitChar));
            AdventureUtil.playerMessage(player, stringBuilder.substring(0, stringBuilder.length()-ConfigReader.Message.splitChar.length()));
        }
    }

    /*
    获取可能的Loot列表
     */
    private List<Loot> getPossibleLootList(FishingCondition fishingCondition) {
        List<Loot> available = new ArrayList<>();
        ConfigReader.LOOT.keySet().forEach(key -> {
            Loot loot = ConfigReader.LOOT.get(key);
            List<Requirement> requirements = loot.getRequirements();
            if (requirements == null){
                available.add(loot);
            }else {
                boolean isMet = true;
                for (Requirement requirement : requirements){
                    if (!requirement.isConditionMet(fishingCondition)){
                        isMet = false;
                    }
                }
                if (isMet){
                    available.add(loot);
                }
            }
        });
        return available;
    }

    private List<Loot> getFinder(FishingCondition fishingCondition) {
        List<Loot> available = new ArrayList<>();
        ConfigReader.LOOT.keySet().forEach(key -> {
            Loot loot = ConfigReader.LOOT.get(key);
            if (!loot.isShowInFinder()) return;
            List<Requirement> requirements = loot.getRequirements();
            if (requirements == null){
                available.add(loot);
            }else {
                boolean isMet = true;
                for (Requirement requirement : requirements){
                    if (!requirement.isConditionMet(fishingCondition)){
                        isMet = false;
                    }
                }
                if (isMet){
                    available.add(loot);
                }
            }
        });
        return available;
    }

    private void showPlayerBar(Player player){
        Loot loot = nextLoot.get(player);

        String layout;
        if (loot.getLayout() != null){
            try {
                layout = loot.getLayout().get((int) (loot.getLayout().size() * Math.random()));
            }
            catch (IndexOutOfBoundsException e){
                AdventureUtil.consoleMessage("<red>[CustomFishing] Layouts should be in a list");
                return;
            }
        }
        else {
            Object[] values = ConfigReader.LAYOUT.keySet().toArray();
            layout = (String) values[new Random().nextInt(values.length)];
        }

        int difficulty = loot.getDifficulty().getSpeed();
        difficulty += Objects.requireNonNullElse(modifiers.get(player).getDifficulty(), 0);;
        if (difficulty < 1){
            difficulty = 1;
        }

        Difficulty difficult = new Difficulty(loot.getDifficulty().getTimer(), difficulty);
        fishingPlayers.put(player,
                new FishingPlayer(System.currentTimeMillis() + loot.getTime(),
                        new Timer(player, difficult, layout)
                )
        );

        if (vanilla.get(player) == null){
            for (ActionB action : loot.getHookActions()){
                action.doOn(player);
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, loot.getTime()/50,3));
    }
}