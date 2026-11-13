package com.pingbar;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PingBarClient implements ClientModInitializer {

    // Design units = pixels of the 1079 px wide Stumble Guys screenshot
    private static final float DESIGN_W = 1079f;
    private static final float BASELINE_Y = 23f;
    private static final float[] BAR_H = {5f, 8f, 10.5f, 12.5f};
    private static final float BAR_W = 3.5f;
    private static final float BAR_PITCH = 5.5f;
    private static final float TEXT_START = 25.4f;
    private static final float MS_GAP = 1.5f;
    private static final float OUTLINE = 0.9f;

    // Tuned for Lilita One at "size": 10 in ping.json
    private static final float BIG_SCALE = 1.736f;
    private static final float MS_SCALE  = 1.0f;
    private static final float BIG_Y = 10.85f;   // BASELINE_Y - 7 * BIG_SCALE
    private static final float MS_Y  = 16.0f;    // BASELINE_Y - 7 * MS_SCALE

    private static final int DIM    = 0x66FFFFFF;
    private static final int GREEN  = 0xFF5CE65C;
    private static final int YELLOW = 0xFFF0D46B;
    private static final int ORANGE = 0xFFF09A3C;
    private static final int RED    = 0xFFE64B4B;

    private static final Style FONT_STYLE =
            Style.EMPTY.withFont(Identifier.of("pingbar", "ping"));

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> render(ctx));
    }

    private void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.options.hudHidden) return;

        PlayerListEntry entry = mc.getNetworkHandler()
                .getPlayerListEntry(mc.player.getUuid());
        int ping = entry == null ? 0 : entry.getLatency();

        int lit; int color;
        if (ping < 40)       { lit = 4; color = GREEN; }
        else if (ping < 100) { lit = 3; color = YELLOW; }
        else if (ping < 200) { lit = 2; color = ORANGE; }
        else                 { lit = 1; color = RED; }

        TextRenderer tr = mc.textRenderer;
        Text big = Text.literal(String.valueOf(ping)).setStyle(FONT_STYLE);
        Text ms  = Text.literal("ms").setStyle(FONT_STYLE);
        float bigW = tr.getWidth(big) * BIG_SCALE;
        float msW  = tr.getWidth(ms) * MS_SCALE;

        float total = TEXT_START + bigW + MS_GAP + msW;
        float left = (DESIGN_W - total) / 2f;
        float s = mc.getWindow().getScaledWidth() / DESIGN_W;

        MatrixStack m = ctx.getMatrices();
        m.push();
        m.scale(s, s, 1f);

        m.push();
        m.scale(0.25f, 0.25f, 1f);
        for (int i = 0; i < 4; i++) {
            float x = left + i * BAR_PITCH;
            int x1 = Math.round(x * 4), x2 = Math.round((x + BAR_W) * 4);
            int y2 = Math.round(BASELINE_Y * 4);
            int y1 = Math.round((BASELINE_Y - BAR_H[i]) * 4);
            ctx.fill(x1, y1, x2, y2, i < lit ? color : DIM);
        }
        m.pop();

        float tx = left + TEXT_START;
        drawOutlined(ctx, tr, big, tx, BIG_Y, BIG_SCALE);
        drawOutlined(ctx, tr, ms, tx + bigW + MS_GAP, MS_Y, MS_SCALE);

        m.pop();
    }

    private static void drawOutlined(DrawContext ctx, TextRenderer tr,
                                     Text t, float x, float y, float scale) {
        MatrixStack m = ctx.getMatrices();
        float[][] o = {{OUTLINE, 0}, {-OUTLINE, 0}, {0, OUTLINE}, {0, -OUTLINE}};
        for (float[] d : o) {
            m.push();
            m.translate(x + d[0], y + d[1], 0);
            m.scale(scale, scale, 1f);
            ctx.drawText(tr, t, 0, 0, 0xCC000000, false);
            m.pop();
        }
        m.push();
        m.translate(x, y, 0);
        m.scale(scale, scale, 1f);
        ctx.drawText(tr, t, 0, 0, 0xFFFFFFFF, false);
        m.pop();
    }
}
