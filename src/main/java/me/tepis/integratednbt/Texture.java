package me.tepis.integratednbt;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Texture {
    private ResourceLocation resourceLocation;

    public Texture(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public Texture(String namespace, String path) {
        this(new ResourceLocation(namespace, path));
    }

    public void bind() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);
    }

    public TexturePart createPart(int x, int y, int width, int height) {
        return new TexturePart(this, x, y, width, height);
    }
}
