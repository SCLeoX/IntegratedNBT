package me.tepis.integratednbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;

public class NBTExtractionOperatorSerializer implements IOperatorSerializer<NBTExtractionOperator> {
    @Override
    public boolean canHandle(IOperator operator) {
        return operator instanceof NBTExtractionOperator;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return NBTExtractionOperator.UNIQUE_NAME;
    }

    @Override
    public Tag serialize(NBTExtractionOperator operator) {
        CompoundTag data = new CompoundTag();
        data.put("path", operator.getExtractionPath().toNBT());
        data.putByte("defaultNBTId", operator.getDefaultNBTId());
        return data;
    }

    @Override
    public NBTExtractionOperator deserialize(ValueDeseralizationContext valueDeseralizationContext, Tag nbt) throws EvaluationException {
        try {
            CompoundTag tag = (CompoundTag) nbt;
            return new NBTExtractionOperator(NBTPath.fromNBT(tag.get("path"))
                .orElse(new NBTPath()), tag.getByte("defaultNBTId"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(Component.literal(e.getMessage()));
        }
    }
}
