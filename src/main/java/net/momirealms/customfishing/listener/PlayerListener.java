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
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.hook.MythicMobsUtils;
import net.momirealms.customfishing.utils.AdventureManager;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.titlebar.Difficulty;
import net.momirealms.customfishing.titlebar.FishingPlayer;
import net.momirealms.customfishing.titlebar.Layout;
import net.momirealms.customfishing.item.Bait;
import net.momirealms.customfishing.item.Loot;
import net.momirealms.customfishing.item.Rod;
import net.momirealms.customfishing.requirements.FishingCondition;
import net.momirealms.customfishing.requirements.Requirement;
import net.momirealms.customfishing.titlebar.Timer;
import net.momirealms.customfishing.utils.Modifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {

    private final HashMap<Player, Long> coolDown = new HashMap<>();
    private final HashMap<Player, Loot> nextLoot = new HashMap<>();
    private final HashMap<Player, Modifier> modifiers = new HashMap<>();
    public static ConcurrentHashMap<Player, FishingPlayer> fishingPlayers = new ConcurrentHashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent event){

        PlayerFishEvent.State state = event.getState();
        Player player = event.getPlayer();

        //抛竿
        if (state.equals(PlayerFishEvent.State.FISHING)){

            //设置冷却时间
            long time = System.currentTimeMillis();
            if (time - (coolDown.getOrDefault(player, time - 2000)) < 2000) {
                return;
            }
            coolDown.put(player, time);

            Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.instance, ()->{

                PlayerInventory inventory = player.getInventory();

                boolean noRod = true;
                double timeModifier = 1;
                double doubleLoot = 0;
                double scoreModifier = 1;
                int difficultyModifier = 0;

                HashMap<String, Integer> pm1 = new HashMap<>();
                HashMap<String, Double> mq1 = new HashMap<>();

                ItemStack mainHandItem = inventory.getItemInMainHand();
                if (mainHandItem.getType() != Material.AIR){
                    NBTItem nbtItem = new NBTItem(inventory.getItemInMainHand());
                    NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                    if (nbtCompound != null){
                        if (nbtCompound.getString("type").equals("rod")) {
                            String key = nbtCompound.getString("id");
                            Rod rod = ConfigReader.ROD.get(key);
                            if (rod != null){
                                pm1 = rod.getWeightPM();
                                mq1 = rod.getWeightMQ();
                                if (rod.getTime() != 0) timeModifier *= rod.getTime();
                                if (rod.getDoubleLoot() != 0) doubleLoot += rod.getDoubleLoot();
                                if (rod.getDifficulty() != 0) difficultyModifier += rod.getDifficulty();
                                if (rod.getScoreModifier() != 0) scoreModifier *= rod.getScoreModifier();
                                noRod = false;
                            }
                        }
                        else if (nbtCompound.getString("type").equals("bait")){
                            String key = nbtCompound.getString("id");
                            Bait bait = ConfigReader.BAIT.get(key);
                            if (bait != null){
                                pm1 = bait.getWeightPM();
                                mq1 = bait.getWeightMQ();
                                if (bait.getTime() != 0) timeModifier *= bait.getTime();
                                if (bait.getDoubleLoot() != 0) doubleLoot += bait.getDoubleLoot();
                                if (bait.getDifficulty() != 0) difficultyModifier += bait.getDifficulty();
                                if (bait.getScoreModifier() != 0) scoreModifier *= bait.getScoreModifier();
                                mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                            }
                        }
                    }
                }

                HashMap<String, Integer> pm2 = new HashMap<>();
                HashMap<String, Double> mq2 = new HashMap<>();

                ItemStack offHandItem = inventory.getItemInOffHand();
                if (offHandItem.getType() != Material.AIR){
                    NBTItem offHand = new NBTItem(inventory.getItemInOffHand());
                    NBTCompound offHandCompound = offHand.getCompound("CustomFishing");
                    if (offHandCompound != null){
                        if (offHandCompound.getString("type").equals("bait")) {
                            String key = offHandCompound.getString("id");
                            Bait bait = ConfigReader.BAIT.get(key);
                            if (bait != null){
                                pm2 = bait.getWeightPM();
                                mq2 = bait.getWeightMQ();
                                if (bait.getTime() != 0) timeModifier *= bait.getTime();
                                if (bait.getDoubleLoot() != 0) doubleLoot += bait.getDoubleLoot();
                                if (bait.getDifficulty() != 0) difficultyModifier += bait.getDifficulty();
                                if (bait.getScoreModifier() != 0) scoreModifier *= bait.getScoreModifier();
                                offHandItem.setAmount(offHandItem.getAmount() - 1);
                            }
                        }else if (noRod && offHandCompound.getString("type").equals("rod")){
                            String key = offHandCompound.getString("id");
                            Rod rod = ConfigReader.ROD.get(key);
                            if (rod != null){
                                pm2 = rod.getWeightPM();
                                mq2 = rod.getWeightMQ();
                                if (rod.getTime() != 0) timeModifier *= rod.getTime();
                                if (rod.getDoubleLoot() != 0) doubleLoot += rod.getDoubleLoot();
                                if (rod.getDifficulty() != 0) difficultyModifier += rod.getDifficulty();
                                if (rod.getScoreModifier() != 0) scoreModifier *= rod.getScoreModifier();
                                noRod = false;
                            }
                        }
                    }
                }

                //是否需要特殊鱼竿
                if (ConfigReader.Config.needSpecialRod && noRod){
                    nextLoot.put(player, null);
                    return;
                }

                //时间修改
                FishHook hook = event.getHook();
                hook.setMaxWaitTime((int) (timeModifier * hook.getMaxWaitTime()));
                hook.setMinWaitTime((int) (timeModifier * hook.getMinWaitTime()));

                //获取抛竿位置处可能的Loot实例列表
                List<Loot> possibleLoots = getPossibleLootList(new FishingCondition(player, hook.getLocation()));
                List<Loot> availableLoots = new ArrayList<>();

                if (possibleLoots.size() == 0){
                    nextLoot.put(player, null);
                    return;
                }

                Modifier modifier = new Modifier();
                modifier.setDifficultyModifier(difficultyModifier);
                modifier.setScoreModifier(scoreModifier);
                modifier.setWillDouble(doubleLoot > Math.random());
                //修改
                modifiers.put(player, modifier);

                double[] weights = new double[possibleLoots.size()];
                int index = 0;
                for (Loot loot : possibleLoots){
                    double weight = loot.getWeight();
                    String group = loot.getGroup();
                    if (group != null){
                        if (pm1 != null && pm1.get(group) != null){
                            weight += pm1.get(group);
                        }
                        if (pm2!= null && pm2.get(group) != null){
                            weight += pm2.get(group);
                        }
                        if (mq1 != null && mq1.get(group) != null){
                            weight *= mq1.get(group);
                        }
                        if (mq2 != null && mq2.get(group) != null){
                            weight *= mq2.get(group);
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
                //以防万一，丢入空值
                nextLoot.put(player, null);
            });
        }

        //咬钩
        else if (state.equals(PlayerFishEvent.State.BITE)){
            //如果没有特殊鱼就返回
            if (nextLoot.get(player) == null) return;
            //如果正在钓一个鱼了，那么也返回
            if (fishingPlayers.get(player) != null) return;

            Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.instance, ()-> {

                Loot lootInstance = nextLoot.get(player);
                //获取布局名，或是随机布局
                String layout = Optional.ofNullable(lootInstance.getLayout()).orElseGet(() ->{
                    Random generator = new Random();
                    Object[] values = ConfigReader.LAYOUT.keySet().toArray();
                    return (String) values[generator.nextInt(values.length)];
                });

                int difficulty = lootInstance.getDifficulty().getSpeed();
                difficulty += Objects.requireNonNullElse(modifiers.get(player).getDifficultyModifier(), 0);;
                if (difficulty < 1){
                    difficulty = 1;
                }
                Difficulty difficult = new Difficulty(lootInstance.getDifficulty().getTimer(), difficulty);

                //根据鱼的时间放入玩家实例，并应用药水效果
                fishingPlayers.put(player,
                        new FishingPlayer(System.currentTimeMillis() + lootInstance.getTime(),
                                new Timer(player, difficult, layout)
                        )
                );
                Bukkit.getScheduler().runTask(CustomFishing.instance, ()->{
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, lootInstance.getTime()/50,3));
                });
            });
        }

        //正常钓到鱼 & 正常收杆
        else if (state.equals(PlayerFishEvent.State.CAUGHT_FISH) || state.equals(PlayerFishEvent.State.REEL_IN)){
            //发现有特殊鱼，那么必须掉落特殊鱼
            if (fishingPlayers.get(player) != null){
                //清除原版战利品
                if (event.getCaught() != null){
                    event.getCaught().remove();
                    event.setExpToDrop(0);
                }
                Loot lootInstance = nextLoot.get(player);
                Layout layout = ConfigReader.LAYOUT.get(fishingPlayers.get(player).getTimer().getLayout());
                int last = (fishingPlayers.get(player).getTimer().getTimerTask().getProgress() + 1)/layout.getRange();
                fishingPlayers.remove(player);
                player.removePotionEffect(PotionEffectType.SLOW);
                if (ConfigReader.Config.needOpenWater && !event.getHook().isInOpenWater()){
                    AdventureManager.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.notOpenWater);
                    return;
                }
                if (Math.random() < layout.getSuccessRate()[last]){
                    //捕鱼成功
                    Location location = event.getHook().getLocation();
                    //钓上来的是MM怪吗
                    if (lootInstance.getMm() != null){
                        MythicMobsUtils.summonMM(player.getLocation(), location, lootInstance);
                    }else {
                        ItemStack itemStack = ConfigReader.LOOTITEM.get(lootInstance.getKey());
                        if (itemStack.getType() != Material.AIR) {
                            Entity item = location.getWorld().dropItem(location, itemStack);
                            Vector vector = player.getLocation().subtract(location).toVector().multiply(0.1);
                            vector = vector.setY((vector.getY()+0.2)*1.2);
                            item.setVelocity(vector);
                            if (modifiers.get(player).willDouble()){
                                Entity item2 = location.getWorld().dropItem(location, itemStack);
                                item2.setVelocity(vector);
                            }
                        }
                    }
                    if (lootInstance.getMsg() != null){
                        //发送消息
                        AdventureManager.playerMessage(player, ConfigReader.Message.prefix + lootInstance.getMsg().replace("{loot}",lootInstance.getNick()).replace("{player}", player.getName()));
                    }
                    if (lootInstance.getCommands() != null){
                        //执行指令
                        lootInstance.getCommands().forEach(command ->{
                            String finalCommand = command.
                                    replaceAll("\\{x}", String.valueOf(Math.round(location.getX()))).
                                    replaceAll("\\{y}", String.valueOf(Math.round(location.getY()))).
                                    replaceAll("\\{z}", String.valueOf(Math.round(location.getZ()))).
                                    replaceAll("\\{player}", event.getPlayer().getName()).
                                    replaceAll("\\{world}", player.getWorld().getName()).
                                    replaceAll("\\{loot}", lootInstance.getNick());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                        });
                    }
                    if (lootInstance.getExp() != 0){
                        player.giveExp(lootInstance.getExp(),true);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
                    }
                    if (lootInstance.getSkillXP() != 0){
                        ConfigReader.Config.skillXP.addXp(player, lootInstance.getSkillXP());
                    }
                    if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()){
                        float score = (float) (lootInstance.getScore() * modifiers.get(player).getScoreModifier());
                        CompetitionSchedule.competition.refreshRanking(player.getName(), score);
                        BossBarManager.joinCompetition(player);
                    }
                    //发送Title
                    AdventureManager.playerTitle(player, ConfigReader.Title.success_title.get((int) (ConfigReader.Title.success_title.size()*Math.random())).replace("{loot}",lootInstance.getNick()), ConfigReader.Title.success_subtitle.get((int) (ConfigReader.Title.success_subtitle.size()*Math.random())).replace("{loot}",lootInstance.getNick()), ConfigReader.Title.success_in, ConfigReader.Title.success_stay, ConfigReader.Title.success_out);
                }else {
                    //移除正在钓鱼的状态
                    fishingPlayers.remove(player);
                    //捕鱼失败Title
                    AdventureManager.playerTitle(player, ConfigReader.Title.failure_title.get((int) (ConfigReader.Title.failure_title.size()*Math.random())), ConfigReader.Title.failure_subtitle.get((int) (ConfigReader.Title.failure_subtitle.size()*Math.random())), ConfigReader.Title.failure_in, ConfigReader.Title.failure_stay, ConfigReader.Title.failure_out);
                }
            }
            //筛选后发现这个地方没有特殊鱼
            else {
                //是否能钓到原版掉落物
                if (!ConfigReader.Config.vanillaDrop){
                    if (event.getCaught() != null){
                        event.getCaught().remove();
                    }
                    event.setExpToDrop(0);
                }
            }
        }
    }

    @EventHandler
    public void onQUit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        player.removePotionEffect(PotionEffectType.SLOW);
        coolDown.remove(player);
        nextLoot.remove(player);
        modifiers.remove(player);
        fishingPlayers.remove(player);
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
                AdventureManager.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.coolDown);
                return;
            }
            coolDown.put(player, time);
            //获取玩家位置处可能的Loot实例列表
            List<Loot> possibleLoots = getFinder(new FishingCondition(player, player.getLocation()));
            if (possibleLoots.size() == 0){
                AdventureManager.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.noLoot);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder(ConfigReader.Message.prefix + ConfigReader.Message.possibleLoots);
            possibleLoots.forEach(loot -> stringBuilder.append(loot.getNick()).append(ConfigReader.Message.splitChar));
            AdventureManager.playerMessage(player, stringBuilder.substring(0, stringBuilder.length()-ConfigReader.Message.splitChar.length()));
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
}