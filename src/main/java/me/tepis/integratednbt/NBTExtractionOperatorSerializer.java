package me.tepis.integratednbt;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;

public class NBTExtractionOperatorSerializer implements IOperatorSerializer<NBTExtractionOperator> {
    @Override
    public boolean canHandle(IOperator operator) {
        return operator instanceof NBTExtractionOperator;
    }

    @Override
    public String getUniqueName() {
        return "integratednbt.nbt_extraction";
    }

    @Override
    public String serialize(NBTExtractionOperator operator) {
        NBTTagCompound data = new NBTTagCompound();
        data.setTag("path", operator.getExtractionPath().toNBT());
        data.setByte("defaultNBTId", operator.getDefaultNBTId());
        return data.toString();
    }

    @Override
    public NBTExtractionOperator deserialize(String value) throws EvaluationException {
        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(value);
            return new NBTExtractionOperator(NBTPath.fromNBT(tag.getTag("path"))
                .orElse(new NBTPath()), tag.getByte("defaultNBTId"));
        } catch (NBTException e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }
}
