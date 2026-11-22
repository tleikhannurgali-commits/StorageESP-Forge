package com.example.storageesp;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Storage ESP ClickGUI — мягкая светло-серая тема с фиолетовыми акцентами.
 * В меню есть плавное появление, переходы вкладок, hover-анимации и тумблеры.
 */
public class ModScreen extends Screen {

    // Полупрозрачное лавандовое стекло: альфа-канал оставляет фон видимым.
    private static final int BG = 0xC9E7E2F1;
    private static final int SIDE_BG = 0xB0D8D0EA;
    private static final int CARD_BG = 0xA8FFFFFF;
    private static final int CARD_HOVER = 0xB8E8DFFF;
    private static final int TEXT = 0xFF342B45;
    private static final int MUTED = 0xFF766D86;
    private static final int PURPLE = 0xFF916BE0;
    private static final int PURPLE_LIGHT = 0xFFD2C1FA;
    private static final int LAVENDER = 0x77C6B2F4;
    private static final int BORDER = 0xCFFFFFFF;

    private static final String[] TAB_NAMES = {"ХРАНИЛИЩА", "ИГРОКИ", "МИР"};
    private static final String[] TAB_HINTS = {
            "Контейнеры, лут и цены",
            "Игроки, радар и метки",
            "Окружение и активность"
    };
    private static final Features[][] TABS = {
            {Features.STORAGE, Features.LOOT, Features.WARDEN, Features.PRICES},
            {Features.PLAYERS, Features.ANTI_INVIS, Features.RADAR, Features.MARKS},
            {Features.VOID_MAP, Features.TRACES}
    };

    private int activeTab = 0;
    private float openProgress = 0.0f;
    private float tabProgress = 1.0f;
    private float tabIndicatorY = -1.0f;
    private float clearHover = 0.0f;
    private float closeHover = 0.0f;
    private final float[] tabHover = new float[TAB_NAMES.length];
    private final float[] featureHover = new float[Features.values().length];
    private final float[] toggleProgress = new float[Features.values().length];
    private long lastFrameTime = 0L;
    private float frameDelta = 1.0f / 60.0f;

    public ModScreen() {
        super(Component.literal("Storage ESP"));
        for (Features f : Features.values()) {
            toggleProgress[f.ordinal()] = f.on() ? 1.0f : 0.0f;
        }
    }

    private int panelWidth() { return Math.min(620, width - 28); }
    private int panelHeight() { return Math.min(420, height - 24); }
    private int panelX() { return (width - panelWidth()) / 2; }
    private int panelY() { return (height - panelHeight()) / 2; }
    private int sideWidth() { return 158; }
    private int contentX() { return panelX() + sideWidth() + 26; }
    private int contentWidth() { return panelWidth() - sideWidth() - 48; }
    private int contentY() { return panelY() + 146; }
    private int cardHeight() { return 82; }
    private int cardGap() { return 12; }
    private int cardWidth() { return (contentWidth() - cardGap()) / 2; }

    private void updateAnimations() {
        long now = System.currentTimeMillis();
        if (lastFrameTime == 0L) lastFrameTime = now;
        frameDelta = Math.min(0.05f, Math.max(0.001f, (now - lastFrameTime) / 1000.0f));
        lastFrameTime = now;

        openProgress = approach(openProgress, 1.0f, frameDelta, 12.0f);
        tabProgress = approach(tabProgress, 1.0f, frameDelta, 9.5f);
        for (Features f : Features.values()) {
            int i = f.ordinal();
            toggleProgress[i] = approach(toggleProgress[i], f.on() ? 1.0f : 0.0f, frameDelta, 15.0f);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        updateAnimations();

        // Настоящее размытие сцены средствами Screen, поверх него — лёгкая вуаль.
        // Так фон мира остаётся различимым под полупрозрачными стеклянными панелями.
        renderBlurredBackground(g);
        int overlayAlpha = Math.round(0x52 * easeOut(openProgress));
        g.fill(0, 0, width, height, (overlayAlpha << 24) | 0x00100B19);

        float reveal = easeOut(openProgress);
        float scale = 0.965f + 0.035f * reveal;
        g.pose().pushMatrix();
        g.pose().translate(width / 2.0f, height / 2.0f);
        g.pose().scale(scale, scale);
        g.pose().translate(-width / 2.0f, -height / 2.0f);

        drawPanel(g, mouseX, mouseY);

        g.pose().popMatrix();
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawPanel(GuiGraphics g, int mouseX, int mouseY) {
        int x = panelX();
        int y = panelY();
        int w = panelWidth();
        int h = panelHeight();

        // Мягкая внешняя тень и несколько слоёв стекла с тонкой светлой кромкой.
        roundRect(g, x - 9, y - 5, w + 18, h + 22, 24, 0x180D0820);
        roundRect(g, x - 4, y + 2, w + 8, h + 10, 21, 0x260D0820);
        roundRect(g, x, y, w, h, 19, 0xBFFFFFFF);
        roundRect(g, x + 1, y + 1, w - 2, h - 2, 18, BG);

        // Стеклянная боковая панель и мягкие отражения на поверхности.
        roundRect(g, x + 1, y + 1, sideWidth() + 4, h - 2, 18, SIDE_BG);
        roundRect(g, x + sideWidth() - 7, y + 1, 10, h - 2, 5, SIDE_BG);
        roundRect(g, x + 3, y + 2, w - 6, 34, 16, 0x26FFFFFF);
        horizontalGradient(g, x + 20, y + 1, w - 40, 2, 0xBFFFFFFF, 0x3CBEA6F2);
        roundRect(g, x + 2, y + 16, 2, h - 32, 1, 0x55FFFFFF);
        roundRect(g, x + w - 4, y + 18, 1, h - 36, 1, 0x35FFFFFF);

        drawHeader(g, x, y, w);
        drawSidebar(g, mouseX, mouseY);
        drawContent(g, mouseX, mouseY);
        drawFooter(g, mouseX, mouseY);
    }

    private void drawHeader(GuiGraphics g, int x, int y, int w) {
        // Небольшой знак из мягких скруглённых полос.
        roundRect(g, x + 20, y + 19, 7, 24, 4, PURPLE);
        roundRect(g, x + 30, y + 13, 7, 30, 4, PURPLE_LIGHT);
        roundRect(g, x + 40, y + 23, 7, 20, 4, 0xFFD2C5EF);

        g.drawString(font, "STORAGE ESP", x + 59, y + 18, TEXT, false);
        g.drawString(font, "VISUAL CONTROL CENTER  /  FORGE 1.21.11", x + 59, y + 33, MUTED, false);

        int tagW = 92;
        int tagX = x + w - tagW - 20;
        roundOutline(g, tagX, y + 19, tagW, 23, 11, 0xAFFFFFFF, 0x88F8F4FF);
        roundRect(g, tagX + 10, y + 28, 5, 5, 3, 0xFF8D70D1);
        g.drawString(font, "CLIENT MOD", tagX + 22, y + 27, 0xFF76658E, false);
        g.fill(x + 20, y + 59, x + w - 20, y + 60, 0xFFE3DFE9);
    }

    private void drawSidebar(GuiGraphics g, int mouseX, int mouseY) {
        int x = panelX() + 15;
        int labelY = panelY() + 83;
        g.drawString(font, "РАЗДЕЛЫ", x + 9, labelY, 0xFF958DA1, false);

        int itemY = labelY + 20;
        int itemW = sideWidth() - 27;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int by = itemY + i * 51;
            boolean selected = activeTab == i;
            boolean hovered = inside(mouseX, mouseY, x, by, itemW, 42);
            tabHover[i] = approach(tabHover[i], hovered ? 1.0f : 0.0f, frameDelta, 13.0f);

            if (selected) {
                float pulse = 0.5f + 0.5f * (float) Math.sin(System.currentTimeMillis() / 650.0);
                roundRect(g, x, by, itemW, 42, 12, 0x78B99FEA);
                roundRect(g, x + 1, by + 1, itemW - 2, 40, 11, 0x42FFFFFF);
                roundRect(g, x + 5, by + 8, 3, 26, 2, PURPLE);
                roundRect(g, x + 13, by + 9, 24, 24, 8, 0xA8D8CDEF);
                g.drawCenteredString(font, tabIcon(i), x + 25, by + 17, 0xFF7657C0);
                g.drawString(font, TAB_NAMES[i], x + 44, by + 16, TEXT, false);
                if (pulse > 0.0f) roundRect(g, x + itemW - 12, by + 18, 4, 4, 2,
                        withAlpha(PURPLE_LIGHT, 0.55f + pulse * 0.35f));
            } else {
                if (tabHover[i] > 0.01f) {
                    roundRect(g, x, by, itemW, 42, 12,
                            lerpColor(0x00000000, 0x88FFFFFF, tabHover[i] * 0.85f));
                }
                roundRect(g, x + 13, by + 9, 24, 24, 8,
                        lerpColor(0x70E4E0EA, 0xA8ECE6F7, tabHover[i]));
                g.drawCenteredString(font, tabIcon(i), x + 25, by + 17,
                        lerpColor(0xFF9A94A5, 0xFF8266C2, tabHover[i]));
                g.drawString(font, TAB_NAMES[i], x + 44, by + 16,
                        lerpColor(0xFF817B8D, 0xFF514663, tabHover[i]), false);
            }
        }

        int dividerY = itemY + TAB_NAMES.length * 51 + 8;
        g.fill(x + 8, dividerY, x + itemW - 8, dividerY + 1, 0xFFDFDAE7);
        g.drawString(font, "МОДУЛЕЙ", x + 9, dividerY + 15, 0xFF958DA1, false);
        roundRect(g, x + 8, dividerY + 33, 28, 24, 8, 0xFFE3DCF2);
        g.drawCenteredString(font, String.valueOf(Features.values().length), x + 22, dividerY + 41, 0xFF7657C0);
        g.drawString(font, "в проекте", x + 43, dividerY + 41, 0xFF898396, false);
    }

    private void drawContent(GuiGraphics g, int mouseX, int mouseY) {
        int x = contentX();
        int y = panelY() + 84;
        float transition = easeOut(tabProgress);
        int titleColor = fadeColor(TEXT, transition);
        int hintColor = fadeColor(MUTED, transition);

        g.drawString(font, TAB_NAMES[activeTab], x, y, titleColor, false);
        g.drawString(font, TAB_HINTS[activeTab], x, y + 16, hintColor, false);

        int enabled = 0;
        for (Features f : TABS[activeTab]) if (f.on()) enabled++;
        String state = enabled + " / " + TABS[activeTab].length + " АКТИВНО";
        int stateX = panelX() + panelWidth() - 20 - font.width(state);
        g.drawString(font, state, stateX, y + 2, fadeColor(0xFF8873B0, transition), false);

        Features[] features = TABS[activeTab];
        for (int i = 0; i < features.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = x + col * (cardWidth() + cardGap());
            int cy = contentY() + row * (cardHeight() + cardGap());
            float entrance = clamp((tabProgress - i * 0.11f) / 0.58f);
            entrance = easeOut(entrance);
            int slide = Math.round((1.0f - entrance) * 8.0f);
            drawFeatureCard(g, features[i], cx, cy + slide, cardWidth(), cardHeight(), mouseX, mouseY, entrance);
        }
    }

    private void drawFeatureCard(GuiGraphics g, Features f, int x, int y, int w, int h,
                                 int mouseX, int mouseY, float entrance) {
        int index = f.ordinal();
        boolean hovered = inside(mouseX, mouseY, x, y, w, h);
        featureHover[index] = approach(featureHover[index], hovered ? 1.0f : 0.0f, frameDelta, 15.0f);
        float hover = featureHover[index];
        float enabled = toggleProgress[index];

        int cardColor = fadeColor(lerpColor(CARD_BG, CARD_HOVER, hover), entrance);
        int borderColor = fadeColor(lerpColor(BORDER, 0xC8D8C3F4, hover), entrance);
        roundOutline(g, x, y, w, h, 13, borderColor, cardColor);
        // Тонкий блик на верхней кромке создаёт эффект стеклянной поверхности.
        roundRect(g, x + 3, y + 2, w - 6, 11, 8, fadeColor(0x32FFFFFF, entrance));
        roundRect(g, x + 2, y + 15, 1, h - 30, 1, fadeColor(0x35FFFFFF, entrance));

        // Иконка с мягкой полупрозрачной подложкой.
        int iconX = x + 12;
        int iconY = y + 23;
        roundRect(g, iconX, iconY, 35, 35, 12,
                fadeColor(lerpColor(0x92EDE9F3, LAVENDER, Math.max(hover, enabled * 0.8f)), entrance));
        g.drawCenteredString(font, iconFor(f), iconX + 17, iconY + 13,
                fadeColor(lerpColor(0xFF8D879A, 0xFF7556BE, enabled), entrance));

        int tx = x + 57;
        int titleColor = fadeColor(lerpColor(TEXT, 0xFF59447F, enabled * 0.55f), entrance);
        int descColor = fadeColor(MUTED, entrance);
        g.drawString(font, shortName(f), tx, y + 25, titleColor, false);
        g.drawString(font, description(f), tx, y + 42, descColor, false);

        // Анимированный тумблер: бегунок плавно переезжает между состояниями.
        int switchW = 36;
        int switchH = 20;
        int sx = x + w - switchW - 12;
        int sy = y + 11;
        int track = fadeColor(lerpColor(0xFFE0DCE6, PURPLE, enabled), entrance);
        roundRect(g, sx, sy, switchW, switchH, 10, track);
        int knobX = sx + 2 + Math.round(enabled * 16.0f);
        roundRect(g, knobX - 1, sy + 1, 18, 18, 9, fadeColor(0x2A3B2A50, entrance));
        roundRect(g, knobX, sy + 1, 16, 16, 8, fadeColor(0xFFFFFFFF, entrance));

        String state = f.on() ? "АКТИВНО" : "ВЫКЛЮЧЕНО";
        g.drawString(font, state, sx - font.width(state) - 8, sy + 6,
                fadeColor(f.on() ? 0xFF8063C1 : 0xFFA29CAC, entrance), false);
    }

    private void drawFooter(GuiGraphics g, int mouseX, int mouseY) {
        int x = panelX();
        int y = panelY() + panelHeight() - 55;
        int w = panelWidth();
        g.fill(x + 20, y, x + w - 20, y + 1, 0xFFE3DFE9);
        g.drawString(font, "RIGHT CTRL  ·  МЕНЮ", x + 22, y + 19, 0xFF918A9D, false);
        g.drawString(font, "ESC  ·  ЗАКРЫТЬ", x + 22, y + 33, 0xFFAAA4B4, false);

        int closeW = 88;
        int clearW = 146;
        int closeX = x + w - 20 - closeW;
        int clearX = closeX - 10 - clearW;
        int by = y + 8;
        clearHover = approach(clearHover, inside(mouseX, mouseY, clearX, by, clearW, 31) ? 1.0f : 0.0f,
                frameDelta, 14.0f);
        closeHover = approach(closeHover, inside(mouseX, mouseY, closeX, by, closeW, 31) ? 1.0f : 0.0f,
                frameDelta, 14.0f);

        roundOutline(g, clearX, by, clearW, 31, 12,
                lerpColor(0xAFFFFFFF, 0xD3C5ED, clearHover),
                lerpColor(0x79FFFFFF, 0xA8EFE9F8, clearHover));
        g.drawCenteredString(font, "ОЧИСТИТЬ ТРЕКЕРЫ", clearX + clearW / 2, by + 11,
                lerpColor(0xFF80798D, 0xFF6D52A9, clearHover));

        roundOutline(g, closeX, by, closeW, 31, 12,
                lerpColor(0xAEBBA9E1, PURPLE, closeHover),
                lerpColor(0x8CECE5F8, 0xB5E1D6F4, closeHover));
        g.drawCenteredString(font, "ГОТОВО", closeX + closeW / 2, by + 11,
                lerpColor(0xFF765BAF, 0xFF6345A5, closeHover));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button != 0) return super.mouseClicked(event, doubleClick);

        int sx = panelX() + 15;
        int sy = panelY() + 83 + 20;
        for (int i = 0; i < TAB_NAMES.length; i++) {
            int by = sy + i * 51;
            if (inside(mouseX, mouseY, sx, by, sideWidth() - 27, 42)) {
                if (activeTab != i) {
                    activeTab = i;
                    tabProgress = 0.0f;
                }
                return true;
            }
        }

        Features[] features = TABS[activeTab];
        for (int i = 0; i < features.length; i++) {
            int cx = contentX() + (i % 2) * (cardWidth() + cardGap());
            int cy = contentY() + (i / 2) * (cardHeight() + cardGap());
            if (inside(mouseX, mouseY, cx, cy, cardWidth(), cardHeight())) {
                features[i].toggle();
                Features.save();
                return true;
            }
        }

        int footerY = panelY() + panelHeight() - 55 + 8;
        int closeX = panelX() + panelWidth() - 20 - 88;
        int clearX = closeX - 10 - 146;
        if (inside(mouseX, mouseY, clearX, footerY, 146, 31)) {
            MarksTracker.clear();
            LootTracker.clear();
            return true;
        }
        if (inside(mouseX, mouseY, closeX, footerY, 88, 31)) {
            onClose();
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        Features.save();
        super.onClose();
    }

    private static void roundOutline(GuiGraphics g, int x, int y, int w, int h, int radius,
                                     int outline, int fill) {
        roundRect(g, x, y, w, h, radius, outline);
        roundRect(g, x + 1, y + 1, w - 2, h - 2, Math.max(1, radius - 1), fill);
    }

    /** Рисует скруглённый прямоугольник небольшим числом горизонтальных полос. */
    private static void roundRect(GuiGraphics g, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (r == 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }

        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + r, y + h - r, color);
        g.fill(x + w - r, y + r, x + w, y + h - r, color);
        for (int row = 0; row < r; row++) {
            double dy = r - row - 0.5;
            int inset = (int) Math.ceil(r - Math.sqrt(Math.max(0.0, r * r - dy * dy)));
            g.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            g.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }

    private static void horizontalGradient(GuiGraphics g, int x, int y, int w, int h, int start, int end) {
        for (int i = 0; i < w; i++) {
            float t = w <= 1 ? 0.0f : (float) i / (w - 1);
            g.fill(x + i, y, x + i + 1, y + h, lerpColor(start, end, t));
        }
    }

    private static float approach(float current, float target, float delta, float speed) {
        float amount = 1.0f - (float) Math.exp(-speed * delta);
        return current + (target - current) * amount;
    }

    private static float easeOut(float t) {
        t = clamp(t);
        float inv = 1.0f - t;
        return 1.0f - inv * inv * inv;
    }

    private static float clamp(float value) { return Math.max(0.0f, Math.min(1.0f, value)); }

    private static int fadeColor(int color, float alpha) {
        int a = (color >>> 24) & 255;
        int scaled = Math.round(a * clamp(alpha));
        return (color & 0x00FFFFFF) | (scaled << 24);
    }

    private static int withAlpha(int color, float alpha) { return fadeColor(color, alpha); }

    private static int lerpColor(int a, int b, float t) {
        t = clamp(t);
        int aa = (a >>> 24) & 255, ar = (a >>> 16) & 255, ag = (a >>> 8) & 255, ab = a & 255;
        int ba = (b >>> 24) & 255, br = (b >>> 16) & 255, bg = (b >>> 8) & 255, bb = b & 255;
        int alpha = Math.round(aa + (ba - aa) * t);
        int red = Math.round(ar + (br - ar) * t);
        int green = Math.round(ag + (bg - ag) * t);
        int blue = Math.round(ab + (bb - ab) * t);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static String tabIcon(int tab) {
        return switch (tab) {
            case 0 -> "ST";
            case 1 -> "PL";
            default -> "WR";
        };
    }

    private static String shortName(Features f) {
        return switch (f) {
            case STORAGE -> "Storage ESP";
            case PLAYERS -> "Player ESP";
            case ANTI_INVIS -> "Anti-Invis";
            case WARDEN -> "Warden Tracker";
            case LOOT -> "Loot Tracker";
            case RADAR -> "Player Radar";
            case MARKS -> "Marks";
            case VOID_MAP -> "Void Map";
            case TRACES -> "Activity Traces";
            case PRICES -> "Price Tags";
        };
    }

    private static String description(Features f) {
        return switch (f) {
            case STORAGE -> "Контейнеры и блоки";
            case PLAYERS -> "Контуры игроков";
            case ANTI_INVIS -> "Видимость невидимых";
            case WARDEN -> "Открытые хранилища";
            case LOOT -> "Уже проверенный лут";
            case RADAR -> "Позиции поблизости";
            case MARKS -> "Точки и смерть";
            case VOID_MAP -> "Подземные пустоты";
            case TRACES -> "Звуковая активность";
            case PRICES -> "Информация о цене";
        };
    }

    private static String iconFor(Features f) {
        return switch (f) {
            case STORAGE -> "ST";
            case PLAYERS -> "PL";
            case ANTI_INVIS -> "AI";
            case WARDEN -> "WD";
            case LOOT -> "LT";
            case RADAR -> "RD";
            case MARKS -> "MK";
            case VOID_MAP -> "VM";
            case TRACES -> "TR";
            case PRICES -> "$";
        };
    }
}
