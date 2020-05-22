package me.tepis.integratednbt;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;

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
    public INBT serialize(NBTExtractionOperator operator) {
        CompoundNBT data = new CompoundNBT();
        data.put("path", operator.getExtractionPath().toNBT());
        data.putByte("defaultNBTId", operator.getDefaultNBTId());
        return data;
    }

    @Override
    public NBTExtractionOperator deserialize(INBT nbt) throws EvaluationException {
        try {
            CompoundNBT tag = (CompoundNBT) nbt;
            return new NBTExtractionOperator(NBTPath.fromNBT(tag.get("path"))
                .orElse(new NBTPath()), tag.getByte("defaultNBTId"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(new StringTextComponent(e.getMessage()));
        }
    }
}
