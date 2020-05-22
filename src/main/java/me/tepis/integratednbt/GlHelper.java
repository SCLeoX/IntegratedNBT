package me.tepis.integratednbt;

import com.mojang.blaze3d.systems.RenderSystem;

public abstract class GlHelper {
    public static void colorInt(int color) {
        RenderSystem.color4f(
            (float) (color >> 16 & 255) / 255.0F,
            (float) (color >> 8 & 255) / 255.0F,
            (float) (color & 255) / 255.0F,
            1
        );
    }
}
