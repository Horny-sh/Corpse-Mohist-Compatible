package de.maxhenkel.corpse.integration.jei;

import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.gui.DeathHistoryScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Main.MODID, "corpse");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(DeathHistoryScreen.class, NoJEIGuiProperties::new);
    }

}