package jp.hakuaserver.hakuapay;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.UUID;

public class JobRewardListener implements Listener {

    private final DatabaseManager db;
    private final FileConfiguration config;

    public JobRewardListener(DatabaseManager db, FileConfiguration config) {
        this.db = db;
        this.config = config;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // デバッグ: 職業を取得
        String job = db.getJob(playerId);
        if (job == null) {
            player.sendMessage("§c職業が設定されていません。");
            return;
        }

        Material block = event.getBlock().getType();
        int reward = config.getInt("jobs." + job + ".actions.break." + block.name().toLowerCase(), 0);


        // 報酬を追加
        db.addBalance(playerId, reward);
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;

        String job = db.getJob(player.getUniqueId());
        if (job == null) return;

        EntityType entity = event.getEntityType();
        int reward = config.getInt("jobs." + job + ".actions.kill." + entity.name().toLowerCase(), 0);
        if (reward > 0) {
            db.addBalance(player.getUniqueId(), reward);
        }
    }

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String job = db.getJob(player.getUniqueId());
        if (job == null) return;

        Material item = event.getRecipe().getResult().getType();
        int reward = config.getInt("jobs." + job + ".actions.craft." + item.name().toLowerCase(), 0);
        if (reward > 0) {
            db.addBalance(player.getUniqueId(), reward);
        }
    }

    @EventHandler
    public void onItemSmelt(org.bukkit.event.inventory.FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        String job = db.getJob(player.getUniqueId());
        if (job == null) return;

        Material item = event.getItemType();
        int reward = config.getInt("jobs." + job + ".actions.smelt." + item.name().toLowerCase(), 0);
        if (reward > 0) {
            db.addBalance(player.getUniqueId(), reward);
        }
    }

    @EventHandler
    public void onPlayerFish(org.bukkit.event.player.PlayerFishEvent event) {
        if (event.getState() != org.bukkit.event.player.PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        String job = db.getJob(player.getUniqueId());
        if (job == null) return;

        if (event.getCaught() instanceof org.bukkit.entity.Item caughtItem) {
            Material item = caughtItem.getItemStack().getType();
            int reward = config.getInt("jobs." + job + ".actions.fish." + item.name().toLowerCase(), 0);
            if (reward > 0) {
                db.addBalance(player.getUniqueId(), reward);
            }
        }
    }
}
