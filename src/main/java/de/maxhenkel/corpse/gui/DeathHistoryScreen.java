package de.maxhenkel.corpse.gui;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corelib.CachedMap;
import de.maxhenkel.corelib.death.Death;
import de.maxhenkel.corelib.inventory.ScreenBase;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.DummyPlayer;
import de.maxhenkel.corpse.net.MessageShowCorpseInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DeathHistoryScreen extends ScreenBase<AbstractContainerMenu> {

    private static final ResourceLocation DEATH_HISTORY_GUI_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/gui_death_history.png");

    private final CachedMap<Death, DummyPlayer> players;

    private Button previous;
    private Button next;

    private List<Death> deaths;
    private int index;
    private int hSplit;

    public DeathHistoryScreen(List<Death> deaths) {
        super(DEATH_HISTORY_GUI_TEXTURE, new DeathHistoryContainer(), Minecraft.getInstance().player.getInventory(), new TranslatableComponent("gui.death_history.corpse.title"));
        this.players = new CachedMap<>(10_000L);
        this.deaths = deaths;
        this.index = 0;

        imageWidth = 248;
        imageHeight = 166;

        hSplit = imageWidth / 2;
    }

    @Override
    protected void init() {
        super.init();

        int padding = 7;
        int buttonWidth = 50;
        int buttonHeight = 20;
        previous = addRenderableWidget(new Button(leftPos + padding, topPos + imageHeight - buttonHeight - padding, buttonWidth, buttonHeight, new TranslatableComponent("button.corpse.previous"), button -> {
            index--;
            if (index < 0) {
                index = 0;
            }
        }));

        addRenderableWidget(new Button(leftPos + (imageWidth - buttonWidth) / 2, topPos + imageHeight - buttonHeight - padding, buttonWidth, buttonHeight, new TranslatableComponent("button.corpse.show_items"), button -> {
            Main.SIMPLE_CHANNEL.sendToServer(new MessageShowCorpseInventory(getCurrentDeath().getPlayerUUID(), getCurrentDeath().getId()));
        }));

        next = addRenderableWidget(new Button(leftPos + imageWidth - buttonWidth - padding, topPos + imageHeight - buttonHeight - padding, buttonWidth, buttonHeight, new TranslatableComponent("button.corpse.next"), button -> {
            index++;
            if (index >= deaths.size()) {
                index = deaths.size() - 1;
            }

        }));
    }

    @Override
    public boolean mouseClicked(double x, double y, int clickType) {
        if (x >= leftPos + 7 && x <= leftPos + hSplit && y >= topPos + 70 && y <= topPos + 100 + font.lineHeight) {
            BlockPos pos = getCurrentDeath().getBlockPos();
            Component teleport = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                    .withStyle((style) -> style
                            .applyFormat(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/execute in " + getCurrentDeath().getDimension() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip")))
                    );
            minecraft.player.sendMessage(new TranslatableComponent("chat.corpse.teleport_death_location", teleport), Util.NIL_UUID);
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
            minecraft.setScreen(null);
        }
        return super.mouseClicked(x, y, clickType);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        Death death = getCurrentDeath();

        // Title
        MutableComponent title = new TranslatableComponent("gui.corpse.death_history.title").withStyle(ChatFormatting.BLACK);
        int titleWidth = font.width(title.getString());
        font.draw(matrixStack, title.getVisualOrderText(), (imageWidth - titleWidth) / 2, 7, 0);

        // Date
        MutableComponent date = new TextComponent(getDate(death.getTimestamp()).getString()).withStyle(ChatFormatting.DARK_GRAY);
        int dateWidth = font.width(date);
        font.draw(matrixStack, date.getVisualOrderText(), (imageWidth - dateWidth) / 2, 20, 0);

        // Name
        drawLeft(matrixStack,
                new TranslatableComponent("gui.corpse.death_history.name")
                        .append(new TextComponent(":"))
                        .withStyle(ChatFormatting.DARK_GRAY),
                40);

        drawRight(matrixStack, new TextComponent(death.getPlayerName()).withStyle(ChatFormatting.GRAY), 40);

        // Dimension
        MutableComponent dimension = new TranslatableComponent("gui.corpse.death_history.dimension")
                .append(new TextComponent(":"))
                .withStyle(ChatFormatting.DARK_GRAY);

        drawLeft(matrixStack, dimension, 55);

        String dimensionName = death.getDimension().split(":")[1];
        boolean shortened = false;

        int dimWidth = font.width(dimension);

        while (dimWidth + font.width(dimensionName + (shortened ? "..." : "")) >= hSplit - 7) {
            dimensionName = dimensionName.substring(0, dimensionName.length() - 1);
            shortened = true;
        }

        drawRight(matrixStack,
                new TranslatableComponent(dimensionName + (shortened ? "..." : "")).withStyle(ChatFormatting.GRAY), 55);

        // Location
        drawLeft(matrixStack,
                new TranslatableComponent("gui.corpse.death_history.location")
                        .append(new TranslatableComponent(":"))
                        .withStyle(ChatFormatting.DARK_GRAY)
                , 70);


        drawRight(matrixStack, new TextComponent(Math.round(death.getPosX()) + " X").withStyle(ChatFormatting.GRAY), 70);
        drawRight(matrixStack, new TextComponent(Math.round(death.getPosY()) + " Y").withStyle(ChatFormatting.GRAY), 85);
        drawRight(matrixStack, new TextComponent(Math.round(death.getPosZ()) + " Z").withStyle(ChatFormatting.GRAY), 100);

        // Player
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        DummyPlayer dummyPlayer = players.get(death, () -> new DummyPlayer(minecraft.level, new GameProfile(death.getPlayerUUID(), death.getPlayerName()), death.getEquipment(), death.getModel()));

        InventoryScreen.renderEntityInInventory((int) (imageWidth * 0.75D), imageHeight / 2 + 30, 40, (int) (leftPos + (imageWidth * 0.75D)) - mouseX, (imageHeight / 2) - mouseY, dummyPlayer);

        if (mouseX >= leftPos + 7 && mouseX <= leftPos + hSplit && mouseY >= topPos + 70 && mouseY <= topPos + 100 + font.lineHeight) {
            renderTooltip(matrixStack, Collections.singletonList(new TranslatableComponent("tooltip.corpse.teleport").getVisualOrderText()), mouseX - leftPos, mouseY - topPos);
        } else if (mouseX >= leftPos + 7 && mouseX <= leftPos + hSplit && mouseY >= topPos + 55 && mouseY <= topPos + 55 + font.lineHeight) {
            renderTooltip(matrixStack, Lists.newArrayList(new TranslatableComponent("gui.corpse.death_history.dimension").getVisualOrderText(), new TextComponent(death.getDimension()).withStyle(ChatFormatting.GRAY).getVisualOrderText()), mouseX - leftPos, mouseY - topPos);

        }
    }

    private static boolean errorShown;

    public static Component getDate(long timestamp) {
        SimpleDateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(new TranslatableComponent("gui.corpse.death_history.date_format").getString());
        } catch (Exception e) {
            if (!errorShown) {
                Main.LOGGER.error("Failed to create date format. This indicates a broken translation: 'gui.corpse.death_history.date_format' translated to {}", new TranslatableComponent("gui.corpse.death_history.date_format").getString());
                errorShown = true;
            }
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
        return new TextComponent(dateFormat.format(new Date(timestamp)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        previous.active = index > 0;

        next.active = index < deaths.size() - 1;
    }

    public void drawLeft(PoseStack matrixStack, MutableComponent text, int height) {
        font.draw(matrixStack, text.getVisualOrderText(), 7, height, 0);
    }

    public void drawRight(PoseStack matrixStack, MutableComponent text, int height) {
        int strWidth = font.width(text);
        font.draw(matrixStack, text.getVisualOrderText(), hSplit - strWidth, height, 0);
    }

    public Death getCurrentDeath() {
        return deaths.get(index);
    }

}
