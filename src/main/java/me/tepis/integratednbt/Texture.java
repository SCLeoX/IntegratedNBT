package me.tepis.integratednbt;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;

public class Texture {
    private ResourceLocation resourceLocation;

    public Texture(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public Texture(String namespace, String path) {
        this(new ResourceLocation(namespace, path));
    }

    public void bind() {
        RenderSystem.setShaderTexture(0, this.resourceLocation);
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public TexturePart createPart(int x, int y, int width, int height) {
        return new TexturePart(this, x, y, width, height);
    }
}
