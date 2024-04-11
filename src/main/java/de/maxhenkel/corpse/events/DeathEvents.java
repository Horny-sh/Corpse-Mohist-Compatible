package de.maxhenkel.corpse.events;

import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.death.DeathManager;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
//---------------------------------------------------------------------------
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

public class DeathEvents {

    //---------------------------------------------------------------------------
    @SubscribeEvent(priority = EventPriority.LOWEST)
    void playerDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) event.getEntity();
        if (Main.SERVER_CONFIG.maxDeathAge.get() != 0) {
            DeathManager.addDeath(player, Death.fromPlayer(player));
        }
        player.level.addFreshEntity(CorpseEntity.createFromDeath(player, Death.fromPlayer(player)));
        new Thread(() -> deleteOldDeaths(player.getLevel())).start();
        event.setCanceled(true);
    }
    //---------------------------------------------------------------------------

    public static void deleteOldDeaths(ServerLevel serverWorld) {
        int ageInDays = Main.SERVER_CONFIG.maxDeathAge.get();
        if (ageInDays < 0) {
            return;
        }
        long ageInMillis = ((long) ageInDays) * 24L * 60L * 60L * 1000L;

        DeathManager.removeDeathsOlderThan(serverWorld, ageInMillis);
    }

}
