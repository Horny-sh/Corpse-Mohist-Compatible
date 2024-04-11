package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.net.MessageRequestDeathHistory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class KeyEvents {

    @SubscribeEvent
    public void onInput(InputEvent.KeyInputEvent event) {
        if (Main.KEY_DEATH_HISTORY.consumeClick()) {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageRequestDeathHistory());
        }
    }

}
