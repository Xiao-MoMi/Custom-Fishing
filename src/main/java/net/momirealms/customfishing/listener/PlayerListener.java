package net.momirealms.customfishing.listener;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.AdventureManager;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.requirements.FishingCondition;
import net.momirealms.customfishing.requirements.Requirement;
import net.momirealms.customfishing.timer.Timer;
import net.momirealms.customfishing.utils.*;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {

    private final HashMap<Player, Long> coolDown = new HashMap<>();
    private final HashMap<Player, LootInstance> nextLoot = new HashMap<>();
    private final HashSet<Player> willDouble = new HashSet<>();
    public static ConcurrentHashMap<Player, FishingPlayer> fishingPlayers = new ConcurrentHashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent event){

        PlayerFishEvent.State state = event.getState();
        Player player = event.getPlayer();

        //抛竿
        if (state.equals(PlayerFishEvent.State.FISHING)){

            //设置冷却时间
            long time = System.currentTimeMillis();
            if (time - (coolDown.getOrDefault(player, time - 1000)) < 1000) {
                return;
            }
            coolDown.put(player, time);

            Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.instance, ()->{
                PlayerInventory inventory = player.getInventory();
                boolean noRod = true;
                double timeModifier = 1;
                double doubleLoot = 0;
                HashMap<String, Integer> pm1 = new HashMap<>();
                HashMap<String, Double> mq1 = new HashMap<>();
                ItemStack mainHandItem = inventory.getItemInMainHand();
                if (mainHandItem.getType() != Material.AIR){
                    NBTItem nbtItem = new NBTItem(inventory.getItemInMainHand());
                    NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                    if (nbtCompound != null){
                        if (nbtCompound.getString("type").equals("rod")) {
                            String key = nbtCompound.getString("id");
                            RodInstance rod = ConfigReader.ROD.get(key);
                            if (rod != null){
                                pm1 = rod.getWeightPM();
                                mq1 = rod.getWeightMQ();
                                if (rod.getTime() != 0) timeModifier *= rod.getTime();
                                if (rod.getDoubleLoot() != 0) doubleLoot += rod.getDoubleLoot();
                                noRod = false;
                            }
                        }
                        else if (nbtCompound.getString("type").equals("bait")){
                            String key = nbtCompound.getString("id");
                            BaitInstance bait = ConfigReader.BAIT.get(key);
                            if (bait != null){
                                pm1 = bait.getWeightPM();
                                mq1 = bait.getWeightMQ();
                                if (bait.getTime() != 0) timeModifier *= bait.getTime();
                                if (bait.getDoubleLoot() != 0) doubleLoot += bait.getDoubleLoot();
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
                            BaitInstance bait = ConfigReader.BAIT.get(key);
                            if (bait != null){
                                pm2 = bait.getWeightPM();
                                mq2 = bait.getWeightMQ();
                                if (bait.getTime() != 0) timeModifier *= bait.getTime();
                                if (bait.getDoubleLoot() != 0) doubleLoot += bait.getDoubleLoot();
                                offHandItem.setAmount(offHandItem.getAmount() - 1);
                            }
                        }else if (noRod && offHandCompound.getString("type").equals("rod")){
                            String key = offHandCompound.getString("id");
                            RodInstance rod = ConfigReader.ROD.get(key);
                            if (rod != null){
                                pm2 = rod.getWeightPM();
                                mq2 = rod.getWeightMQ();
                                if (rod.getTime() != 0) timeModifier *= rod.getTime();
                                if (rod.getDoubleLoot() != 0) doubleLoot += rod.getDoubleLoot();
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

                FishHook hook = event.getHook();
                hook.setMaxWaitTime((int) (timeModifier * hook.getMaxWaitTime()));
                hook.setMinWaitTime((int) (timeModifier * hook.getMinWaitTime()));

                //获取抛竿位置处可能的Loot实例列表
                List<LootInstance> possibleLoots = getPossibleLootList(new FishingCondition(player, hook.getLocation()));
                List<LootInstance> availableLoots = new ArrayList<>();
                if (possibleLoots.size() == 0){
                    nextLoot.put(player, null);
                    return;
                }

                double[] weights = new double[possibleLoots.size()];
                int index = 0;
                for (LootInstance loot : possibleLoots){
                    double weight = loot.getWeight();
                    String group =loot.getGroup();
                    if (group != null){
                        if (pm1.get(group) != null){
                            weight += pm1.get(group);
                        }
                        if (pm2.get(group) != null){
                            weight += pm2.get(group);
                        }
                        if (mq1.get(group) != null){
                            weight *= mq1.get(group);
                        }
                        if (mq2.get(group) != null){
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
                //根据随机数所落区间获取Loot实例
                double random = Math.random();
                int pos = Arrays.binarySearch(weightRange, random);

                if (pos < 0) {
                    //二分法，数组中不存在该元素，则会返回 -(插入点 + 1)
                    pos = -pos - 1;
                } else {
                    //如果存在，那真是中大奖了！
                    if (doubleLoot > Math.random()) {
                        willDouble.add(player);
                    }else {
                        willDouble.remove(player);
                    }
                    nextLoot.put(player, availableLoots.get(pos));
                    return;
                }
                if (pos < weightRange.length && random < weightRange[pos]) {
                    if (doubleLoot > Math.random()) {
                        willDouble.add(player);
                    }else {
                        willDouble.remove(player);
                    }
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

                LootInstance lootInstance = nextLoot.get(player);
                //获取布局名，或是随机布局
                String layout = Optional.ofNullable(lootInstance.getLayout()).orElseGet(() ->{
                    Random generator = new Random();
                    Object[] values = ConfigReader.LAYOUT.keySet().toArray();
                    return (String) values[generator.nextInt(values.length)];
                });

                PlayerInventory playerInventory = player.getInventory();
                ItemStack mainHandItem = playerInventory.getItemInMainHand();
                if (mainHandItem.getType() != Material.AIR){
                    NBTItem nbtItem = new NBTItem(player.getInventory().getItemInMainHand());
                    NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                    if (nbtCompound != null){
                        String rodKey = nbtCompound.getString("id");
                        RodInstance rod = ConfigReader.ROD.get(rodKey);
                        if (rod != null){
                            if (rod.getDifficulty() != 0) {
                                int difficulty = lootInstance.getDifficulty().getSpeed();
                                difficulty += rod.getDifficulty();
                                if (difficulty < 1){
                                    difficulty = 1;
                                }
                                Difficulty difficult = new Difficulty(lootInstance.getDifficulty().getTimer(), difficulty);
                                fishingPlayers.put(player,
                                        new FishingPlayer(System.currentTimeMillis() + lootInstance.getTime(),
                                                new Timer(player, difficult, layout)
                                        )
                                );
                                return;
                            }
                        }
                    }
                }
                ItemStack offHandItem = playerInventory.getItemInOffHand();
                if (offHandItem.getType() != Material.AIR){
                    NBTItem nbtItem = new NBTItem(player.getInventory().getItemInOffHand());
                    NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                    if (nbtCompound != null){
                        String rodKey = nbtCompound.getString("id");
                        RodInstance rod = ConfigReader.ROD.get(rodKey);
                        if (rod != null){
                            if (rod.getDifficulty() != 0) {
                                int difficulty = lootInstance.getDifficulty().getSpeed();
                                difficulty += rod.getDifficulty();
                                if (difficulty < 1){
                                    difficulty = 1;
                                }
                                Difficulty difficult = new Difficulty(lootInstance.getDifficulty().getTimer(), difficulty);
                                fishingPlayers.put(player,
                                        new FishingPlayer(System.currentTimeMillis() + lootInstance.getTime(),
                                                new Timer(player, difficult, layout)
                                        )
                                );
                                return;
                            }
                        }
                    }
                }

                //根据鱼的时间放入玩家实例，并应用药水效果
                fishingPlayers.put(player,
                        new FishingPlayer(System.currentTimeMillis() + lootInstance.getTime(),
                                new Timer(player, lootInstance.getDifficulty(), layout)
                        )
                );
                Bukkit.getScheduler().callSyncMethod(CustomFishing.instance, ()->{
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, lootInstance.getTime()/50,3));
                    return null;
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
                LootInstance lootInstance = nextLoot.get(player);
                LayoutUtil layout = ConfigReader.LAYOUT.get(fishingPlayers.get(player).getTimer().getLayout());
                int last = (fishingPlayers.get(player).getTimer().getTimerTask().getProgress() + 1)/layout.getRange();
                fishingPlayers.remove(player);
                player.removePotionEffect(PotionEffectType.SLOW);
                if (!event.getHook().isInOpenWater()){
                    AdventureManager.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.notOpenWater);
                    return;
                }
                if (Math.random() < layout.getSuccessRate()[last]){
                    //捕鱼成功
                    Location location = event.getHook().getLocation();
                    //钓上来的是MM怪吗
                    if (lootInstance.getMm() != null){
                        MMUtil.summonMM(player.getLocation(), location, lootInstance);
                    }else {
                        Entity item = location.getWorld().dropItem(location, ConfigReader.LOOTITEM.get(lootInstance.getKey()));
                        Vector vector = player.getLocation().subtract(location).toVector().multiply(0.1);
                        vector = vector.setY((vector.getY()+0.2)*1.2);
                        item.setVelocity(vector);
                        if (willDouble.contains(player)){
                            Entity item2 = location.getWorld().dropItem(location, ConfigReader.LOOTITEM.get(lootInstance.getKey()));
                            item2.setVelocity(vector);
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
        willDouble.remove(player);
        fishingPlayers.remove(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if (!event.hasItem()) return;
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        ItemStack itemStack = event.getItem();
        if (itemStack.getType() == Material.AIR) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getCompound("CustomFishing") == null) return;
        if (nbtItem.getCompound("CustomFishing").getString("type").equals("util") || nbtItem.getCompound("CustomFishing").getString("id").equals("fishfinder")){
            Player player = event.getPlayer();
            //设置冷却时间
            long time = System.currentTimeMillis();
            if (time - (coolDown.getOrDefault(player, time - ConfigReader.Config.fishFinderCoolDown)) < ConfigReader.Config.fishFinderCoolDown) {
                AdventureManager.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.coolDown);
                return;
            }
            coolDown.put(player, time);
            //获取玩家位置处可能的Loot实例列表
            List<LootInstance> possibleLoots = getPossibleLootList(new FishingCondition(player, player.getLocation()));
            if (possibleLoots.size() == 0){
                AdventureManager.playerMessage(player, ConfigReader.Message.prefix + ConfigReader.Message.noLoot);
                return;
            }
            StringBuilder stringBuilder = new StringBuilder(ConfigReader.Message.prefix + ConfigReader.Message.possibleLoots);
            possibleLoots.forEach(loot -> stringBuilder.append(loot.getNick()).append(ConfigReader.Message.splitChar));
            AdventureManager.playerMessage(player, stringBuilder.substring(0, stringBuilder.length()-1));
        }
    }

    /*
    获取可能的Loot列表
     */
    private List<LootInstance> getPossibleLootList(FishingCondition fishingCondition) {
        List<LootInstance> available = new ArrayList<>();
        ConfigReader.LOOT.keySet().forEach(key -> {
            LootInstance loot = ConfigReader.LOOT.get(key);
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

//            //计算总权重
//            double totalWeight = 0;
//            for (LootInstance loot : possibleLoots){
//                double weight = loot.getWeight();
//                String group = loot.getGroup();
//                if (group != null){
//                    if (pm.get(group) != null){
//                        weight += pm.get(group);
//                    }
//                    if (pm2.get(group) != null){
//                        weight += pm2.get(group);
//                    }
//                    if (mq.get(group) != null){
//                        weight *= mq.get(group);
//                    }
//                    if (mq2.get(group) != null){
//                        weight *= mq2.get(group);
//                    }
//                }
//                //需要进行weight修改
//                if (weight <= 0) continue;
//                availableLoots.add(loot);
//                totalWeight += loot.getWeight();
//            }
//计算每种鱼权重所占的比例并输入数组
//            double[] weightRatios = new double[possibleLoots.size()];
//            int index = 0;
//            for (LootInstance loot : possibleLoots) {
//                double weight = loot.getWeight();
//                String group = loot.getGroup();
//                if (group != null){
//                    if (pm.get(group) != null){
//                        weight += pm.get(group);
//                    }
//                    if (pm2.get(group) != null){
//                        weight += pm2.get(group);
//                    }
//                    if (mq.get(group) != null){
//                        weight *= mq.get(group);
//                    }
//                    if (mq2.get(group) != null){
//                        weight *= mq2.get(group);
//                    }
//                }
//                //需要进行weight修改
//                if (weight <= 0) continue;
//                weightRatios[index++] = weight / totalWeight;
//            }
//            //根据权重比例划分定义域
//            double[] weights = new double[availableLoots.size()];
//            double startPos = 0;
//            for (int i = 0; i < index; i++) {
//                weights[i] = startPos + weightRatios[i];
//                startPos += weightRatios[i];
//            }