package com.example.storageesp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * "Warden": ищет сундуки в данже, над которыми НЕТ голограммы с таймером вида 02:15.
 * Нет таймера = сундук уже открылся, на него ставится зелёная метка.
 *
 * Голограммы читаются по имени сущности (подставки для брони с кастомным именем).
 * Если на сервере голограммы сделаны текстовыми дисплеями, чтение текста нужно будет подправить.
 */
public class WardenTracker {

    /** Формат таймера: M:SS или MM:SS. */
    private static final Pattern TIMER = Pattern.compile("\\b\\d{1,2}:\\d{2}\\b");

    /** Ищем только вблизи: дальше голограммы могут быть ещё не загружены у клиента. */
    private static final double RANGE = 48.0;
    private static final int RANGE_CHUNKS = 4;

    private static final int OPEN_COLOR = 0xFF00FF66;
    private static final float LINE_WIDTH = 4.0f;

    public static void draw(Minecraft mc, ClientLevel level) {
        // 1. Собираем позиции всех голограмм с таймером
        List<Vec3> timers = new ArrayList<>();
        for (Entity e : level.entitiesForRendering()) {
            Component name = e.getCustomName();
            if (name != null && TIMER.matcher(name.getString()).find()) {
                timers.add(e.position());
            }
        }

        // 2. Проверяем каждый сундук рядом: есть ли над ним таймер
        Vec3 pp = mc.player.position();
        double rangeSq = RANGE * RANGE;
        int pcx = mc.player.blockPosition().getX() >> 4;
        int pcz = mc.player.blockPosition().getZ() >> 4;

        for (int cx = pcx - RANGE_CHUNKS; cx <= pcx + RANGE_CHUNKS; cx++) {
            for (int cz = pcz - RANGE_CHUNKS; cz <= pcz + RANGE_CHUNKS; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof ChestBlockEntity)) continue;
                    BlockPos pos = be.getBlockPos();
                    if (pos.distToCenterSqr(pp.x, pp.y, pp.z) > rangeSq) continue;
                    if (hasTimerAbove(pos, timers)) continue;
                    Gizmos.cuboid(new AABB(pos).inflate(0.02), GizmoStyle.stroke(OPEN_COLOR, LINE_WIDTH))
                            .setAlwaysOnTop();
                }
            }
        }
    }

    private static boolean hasTimerAbove(BlockPos pos, List<Vec3> timers) {
        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        for (Vec3 t : timers) {
            double dy = t.y - pos.getY();
            if (Math.abs(t.x - cx) <= 1.2 && Math.abs(t.z - cz) <= 1.2 && dy > -0.5 && dy <= 3.5) {
                return true;
            }
        }
        return false;
    }
}
