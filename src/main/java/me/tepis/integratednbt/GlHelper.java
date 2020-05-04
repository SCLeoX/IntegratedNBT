package me.tepis.integratednbt;

import net.minecraft.client.renderer.GlStateManager;

public abstract class GlHelper {
    public static void colorInt(int color) {
        GlStateManager.color(
            (float) (color >> 16 & 255) / 255.0F,
            (float) (color >> 8 & 255) / 255.0F,
            (float) (color & 255) / 255.0F,
            1
        );
    }
}
