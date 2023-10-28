package me.tepis.integratednbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;

import java.util.Optional;

public class NBTExtractedVariableFacadeHandler
    implements IVariableFacadeHandler<NBTExtractedVariableFacade> {
    private static final String KEY_SOURCE_NBT_ID = "sourceNBTId";
    private static final String KEY_EXTRACTION_PATH = "extractionPath";
    private static final String KEY_DEFAULT_NBT_ID = "defaultNBTId";
    private static NBTExtractedVariableFacadeHandler instance;

    public static NBTExtractedVariableFacadeHandler getInstance() {
        if (instance == null) {
            instance = new NBTExtractedVariableFacadeHandler();
        }
        return instance;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return new ResourceLocation(IntegratedNBT.MODID, "nbt_extracted");
    }

    @Override
    public NBTExtractedVariableFacade getVariableFacade(ValueDeseralizationContext valueDeseralizationContext, int id, CompoundTag tag) {
        int sourceNBTId = tag.getInt(KEY_SOURCE_NBT_ID);
        Optional<NBTPath> extractionPath = NBTPath.fromNBT(tag.get(KEY_EXTRACTION_PATH));
        byte defaultNBTId = tag.getByte(KEY_DEFAULT_NBT_ID);
        return new NBTExtractedVariableFacade(
            id,
            sourceNBTId,
            extractionPath.orElse(null),
            defaultNBTId
        );
    }

    @Override
    public void setVariableFacade(CompoundTag tag, NBTExtractedVariableFacade facade) {
        tag.putInt(KEY_SOURCE_NBT_ID, facade.getSourceNBTId());
        tag.put(KEY_EXTRACTION_PATH, facade.getExtractionPath().toNBT());
        tag.putByte(KEY_DEFAULT_NBT_ID, facade.getDefaultNBTId());
    }
}
