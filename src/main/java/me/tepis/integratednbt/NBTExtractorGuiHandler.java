package me.tepis.integratednbt;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class NBTExtractorGuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(
        int id,
        EntityPlayer player,
        World world,
        int x,
        int y,
        int z
    ) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity instanceof NBTExtractorTileEntity) {
            return new NBTExtractorContainer(player.inventory, (NBTExtractorTileEntity) tileEntity);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(
        int ID,
        EntityPlayer player,
        World world,
        int x,
        int y,
        int z
    ) {
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (tileEntity != null) {
            return new NBTExtractorGui(new NBTExtractorContainer(
                player.inventory,
                (NBTExtractorTileEntity) tileEntity
            ));
        }
        return null;
    }
}
