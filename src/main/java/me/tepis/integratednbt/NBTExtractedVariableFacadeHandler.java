package me.tepis.integratednbt;

import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;

import java.util.Optional;

public class NBTExtractedVariableFacadeHandler
    implements IVariableFacadeHandler<NBTExtractedVariableFacade> {
    private static final String VARIABLE_TYPE_ID = "integratednbt:nbt_extracted";
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
    public String getTypeId() {
        return VARIABLE_TYPE_ID;
    }

    @Override
    public NBTExtractedVariableFacade getVariableFacade(int id, NBTTagCompound tag) {
        int sourceNBTId = tag.getInteger(KEY_SOURCE_NBT_ID);
        Optional<NBTPath> extractionPath = NBTPath.fromNBT(tag.getTag(KEY_EXTRACTION_PATH));
        byte defaultNBTId = tag.getByte(KEY_DEFAULT_NBT_ID);
        return new NBTExtractedVariableFacade(
            id,
            sourceNBTId,
            extractionPath.orElse(null),
            defaultNBTId
        );
    }

    @Override
    public void setVariableFacade(NBTTagCompound tag, NBTExtractedVariableFacade facade) {
        tag.setInteger(KEY_SOURCE_NBT_ID, facade.getSourceNBTId());
        tag.setTag(KEY_EXTRACTION_PATH, facade.getExtractionPath().toNBT());
        tag.setByte(KEY_DEFAULT_NBT_ID, facade.getDefaultNBTId());
    }
}
