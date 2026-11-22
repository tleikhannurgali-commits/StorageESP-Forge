package com.example.storageesp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Метки:
 *  - призраки: где ты в последний раз видел игрока, который пропал из зоны видимости (60 секунд);
 *  - точка смерти;
 *  - свои метки (клавиша M).
 * Рисуются вертикальным лучом сквозь стены.
 */
public class MarksTracker {

    public record Mark(String label, Vec3 pos, int color) {}
    public record Ghost(String name, Vec3 pos, long tick) {}

    private static final long GHOST_TTL = 20L * 60;
    private static final double BEAM_HEIGHT = 40.0;

    private static final List<Mark> marks = new ArrayList<>();
    private static final Map<UUID, Ghost> seen = new HashMap<>();
    private static final List<Ghost> activeGhosts = new ArrayList<>();
    private static boolean wasDead;
    private static Object lastLevel;

    public static List<Mark> marks() { return marks; }

    public static List<Ghost> ghosts() { return activeGhosts; }

    public static void clear() {
        marks.clear();
        seen.clear();
        activeGhosts.clear();
    }

    public static void addMark(Minecraft mc) {
        marks.add(new Mark("Метка " + (marks.size() + 1), mc.player.position(), 0xFFFFDD33));
        mc.player.displayClientMessage(Component.literal("Метка поставлена"), true);
    }

    public static void tick(Minecraft mc, ClientLevel level) {
        if (lastLevel != level) {
            clear();
            lastLevel = level;
            wasDead = false;
        }
        long now = level.getGameTime();

        for (AbstractClientPlayer p : level.players()) {
            if (p == mc.player) continue;
            seen.put(p.getUUID(), new Ghost(p.getName().getString(), p.position(), now));
        }
        seen.values().removeIf(g -> now - g.tick() > GHOST_TTL);

        // Призрак = игрок, которого мы видели раньше, но сейчас он не в зоне видимости
        activeGhosts.clear();
        for (Ghost g : seen.values()) {
            if (g.tick() != now) activeGhosts.add(g);
        }

        boolean dead = mc.player.isDeadOrDying();
        if (dead && !wasDead) {
            marks.add(new Mark("Смерть", mc.player.position(), 0xFFFF4444));
        }
        wasDead = dead;

        for (Mark m : marks) beam(m.pos(), m.color());
        for (Ghost g : activeGhosts) beam(g.pos(), 0xFF33DDFF);
    }

    private static void beam(Vec3 p, int color) {
        AABB box = new AABB(p.x - 0.2, p.y, p.z - 0.2, p.x + 0.2, p.y + BEAM_HEIGHT, p.z + 0.2);
        Gizmos.cuboid(box, GizmoStyle.stroke(color, 3.0f)).setAlwaysOnTop();
    }
}
