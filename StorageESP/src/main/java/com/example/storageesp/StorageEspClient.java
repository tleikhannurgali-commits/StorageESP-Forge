package com.example.storageesp;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import org.lwjgl.glfw.GLFW;

/**
 * Главный клиентский класс: клавиши, тик, подписки на события.
 * Рамки рисуются через Gizmos с setAlwaysOnTop() — видны сквозь стены.
 * Gizmos отправляются раз в тик (20 раз в секунду).
 *
 * Клавиши: Right Control — меню; H/J/K/O — быстрые переключатели; M — поставить метку.
 */
public class StorageEspClient {

    /** Радиус поиска хранилищ в чанках (6 чанков = 96 блоков). */
    private static final int RANGE_CHUNKS = 6;
    private static final float LINE_WIDTH = 2.5f;

    private static final KeyMapping KEY_MENU = key("menu", GLFW.GLFW_KEY_RIGHT_CONTROL);
    private static final KeyMapping KEY_STORAGE = key("storage", GLFW.GLFW_KEY_H);
    private static final KeyMapping KEY_PLAYERS = key("players", GLFW.GLFW_KEY_J);
    private static final KeyMapping KEY_INVIS = key("invis", GLFW.GLFW_KEY_K);
    private static final KeyMapping KEY_WARDEN = key("warden", GLFW.GLFW_KEY_O);
    private static final KeyMapping KEY_MARK = key("mark", GLFW.GLFW_KEY_M);

    private static KeyMapping key(String name, int code) {
        return new KeyMapping("key.storageesp." + name, InputConstants.Type.KEYSYM, code, KeyMapping.Category.MISC);
    }

    public static void init(BusGroup modBus) {
        Features.load();
        RegisterKeyMappingsEvent.getBus(modBus).addListener(StorageEspClient::onRegisterKeys);
        TickEvent.ClientTickEvent.Post.BUS.addListener(StorageEspClient::onClientTick);
        AddGuiOverlayLayersEvent.BUS.addListener(StorageEspClient::onRegisterHudLayer);
        ItemTooltipEvent.BUS.addListener(PriceTags::onTooltip);
        PlaySoundEvent.BUS.addListener(TracesTracker::onSound);
    }

    private static void onRegisterHudLayer(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(
            Identifier.fromNamespaceAndPath("storageesp", "radar"),
            RadarHud::onRender
        );
    }

    private static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_MENU);
        event.register(KEY_STORAGE);
        event.register(KEY_PLAYERS);
        event.register(KEY_INVIS);
        event.register(KEY_WARDEN);
        event.register(KEY_MARK);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;

        while (KEY_MENU.consumeClick()) { if (mc.screen == null) mc.setScreen(new ModScreen()); }
        while (KEY_STORAGE.consumeClick()) toggle(mc, Features.STORAGE);
        while (KEY_PLAYERS.consumeClick()) toggle(mc, Features.PLAYERS);
        while (KEY_INVIS.consumeClick())   toggle(mc, Features.ANTI_INVIS);
        while (KEY_WARDEN.consumeClick())  toggle(mc, Features.WARDEN);
        while (KEY_MARK.consumeClick())    MarksTracker.addMark(mc);

        FullbrightController.tick(mc);

        if (Features.ANTI_INVIS.on()) revealInvisiblePlayers(level);
        if (Features.PLAYERS.on()) drawPlayers(mc, level);
        if (Features.LOOT.on()) LootTracker.tick(mc, level);
        if (Features.STORAGE.on()) drawStorage(mc, level);
        if (Features.WARDEN.on()) WardenTracker.draw(mc, level);
        if (Features.MARKS.on()) MarksTracker.tick(mc, level);
        if (Features.VOID_MAP.on()) VoidMapScanner.tick(mc, level);
        if (Features.TRACES.on()) TracesTracker.draw();
    }

    private static void toggle(Minecraft mc, Features f) {
        f.toggle();
        Features.save();
        mc.player.displayClientMessage(Component.literal(f.label + ": " + (f.on() ? "ON" : "OFF")), true);
    }

    /** Снимает флаг невидимости у игроков на клиенте — они снова рисуются. */
    private static void revealInvisiblePlayers(ClientLevel level) {
        for (AbstractClientPlayer p : level.players()) {
            if (p.isInvisible()) p.setInvisible(false);
        }
    }

    private static void drawPlayers(Minecraft mc, ClientLevel level) {
        for (AbstractClientPlayer p : level.players()) {
            if (p == mc.player) continue;
            // Союзники по команде — зелёные, остальные — красные
            int color = p.isAlliedTo(mc.player) ? 0xFF55FF55 : 0xFFFF3333;
            Gizmos.cuboid(p.getBoundingBox(), GizmoStyle.stroke(color, LINE_WIDTH)).setAlwaysOnTop();
            if (EspSettings.playerTracer) {
                Gizmos.line(mc.player.getEyePosition(), p.position().add(0.0, p.getBbHeight() * 0.5, 0.0), color, 1.8f)
                        .setAlwaysOnTop();
            }
            int textRow = 0;
            if (EspSettings.playerHealth) {
                Gizmos.billboardTextOverMob(p, textRow++, "HP: " + Math.round(p.getHealth()), 0xFF8EFF86, 0.72f);
            }
            if (EspSettings.playerDistance) {
                int meters = (int) Math.round(mc.player.distanceTo(p));
                Gizmos.billboardTextOverMob(p, textRow, meters + " m", 0xFFFFFFFF, 0.72f);
            }
        }
    }

    private static void drawStorage(Minecraft mc, ClientLevel level) {
        int pcx = mc.player.blockPosition().getX() >> 4;
        int pcz = mc.player.blockPosition().getZ() >> 4;
        boolean lootOn = Features.LOOT.on();
        for (int cx = pcx - RANGE_CHUNKS; cx <= pcx + RANGE_CHUNKS; cx++) {
            for (int cz = pcz - RANGE_CHUNKS; cz <= pcz + RANGE_CHUNKS; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    int color = colorFor(be);
                    if (color == 0) continue;
                    // Уже открытые тобой хранилища — серые
                    if (lootOn && LootTracker.isLooted(be.getBlockPos())) color = 0xFF777777;
                    AABB box = new AABB(be.getBlockPos()).inflate(0.002);
                    Gizmos.cuboid(box, GizmoStyle.stroke(color, LINE_WIDTH)).setAlwaysOnTop();
                    if (EspSettings.storageTracer) {
                        Vec3 target = Vec3.atCenterOf(be.getBlockPos());
                        if (mc.player.getEyePosition().distanceToSqr(target) <= 64.0 * 64.0) {
                            Gizmos.line(mc.player.getEyePosition(), target, color, 1.25f).setAlwaysOnTop();
                        }
                    }
                }
            }
        }
    }

    /** ARGB-цвет рамки для типа блока, либо 0 — не подсвечивать. */
    private static int colorFor(BlockEntity be) {
        if (be instanceof TrappedChestBlockEntity)    return 0xFFFF2626; // красный
        if (be instanceof ChestBlockEntity)           return 0xFFFFA619; // оранжевый
        if (be instanceof EnderChestBlockEntity)      return 0xFFB333FF; // фиолетовый
        if (be instanceof BarrelBlockEntity)          return 0xFF996633; // коричневый
        if (be instanceof ShulkerBoxBlockEntity shulker) {
            DyeColor dye = shulker.getColor();
            return dye != null ? (0xFF000000 | dye.getTextureDiffuseColor()) : 0xFF9966B3;
        }
        if (be instanceof HopperBlockEntity)          return 0xFF999999; // серый
        if (be instanceof DispenserBlockEntity)       return 0xFF4DCCFF; // голубой (и дроппер)
        if (be instanceof AbstractFurnaceBlockEntity) return 0xFFE6E64D; // жёлтый
        return 0;
    }
}
