package me.tepis.integratednbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import org.apache.commons.lang3.ArrayUtils;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeDouble.ValueDouble;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger.ValueInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeList.ValueList;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeLong.ValueLong;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeString.ValueString;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class NBTValueConverter {
    public static IValueType<? extends IValue> mapNBTToValueType(NBTBase nbt) {
        switch (nbt.getId()) {
            case 1: // Byte
            case 2: // Short
            case 3: // Int
                return ValueTypes.INTEGER;
            case 4: // Long
                return ValueTypes.LONG;
            case 5: // Float
            case 6: // Double
                return ValueTypes.DOUBLE;
            case 7: // Byte Array
            case 9: // List
            case 11: // Int Array
            case 12: // Long Array
                return ValueTypes.LIST;
            case 8: // String
                return ValueTypes.STRING;
            case 10: // Compound
                return ValueTypes.NBT;
            default:
                throw new RuntimeException("Unexpected NBT id:" + nbt.getId());
        }
    }

    public static String getDefaultValueDisplayText(int nbtId) {
        String formatCode = getDefaultValue(nbtId).getType().getDisplayColorFormat();
        switch (nbtId) {
            case 1: // Byte
            case 2: // Short
            case 3: // Int
            case 4: // Long
            case 5: // Float
            case 6: // Double
            default:
                return formatCode + "0";
            case 7: // Byte Array
            case 9: // List
            case 11: // Int Array
            case 12: // Long Array
                return formatCode + "[]";
            case 8: // String
                return formatCode + "\"\"";
            case 10: // Compound
                return formatCode + "{}";

        }
    }

    public static IValue getDefaultValue(int nbtId) {
        switch (nbtId) {
            case 1: // Byte
            case 2: // Short
            case 3: // Int
                return ValueInteger.of(0);
            case 4: // Long
                return ValueLong.of(0);
            case 5: // Float
            case 6: // Double
                return ValueDouble.of(0);
            case 7: // Byte Array
            case 11: // Int Array
            case 12: // Long Array
                return ValueList.ofList(ValueTypes.INTEGER, new ArrayList<>());
            case 8: // String
                return ValueString.of("");
            case 9: // List
                return ValueList.ofList(ValueTypes.CATEGORY_ANY, new ArrayList<>());
            case 10: // Compound
            default:
                return ValueNbt.of(new NBTTagCompound());
        }
    }

    public static IValue mapNBTToValue(NBTBase nbt) {
        switch (nbt.getId()) {
            case 1: // Byte
                return ValueInteger.of(((NBTTagByte) nbt).getInt());
            case 2: // Short
                return ValueInteger.of(((NBTTagShort) nbt).getInt());
            case 3: // Int
                return ValueInteger.of(((NBTTagInt) nbt).getInt());
            case 4: // Long
                return ValueLong.of(((NBTTagLong) nbt).getLong());
            case 5: // Float
                return ValueDouble.of(((NBTTagFloat) nbt).getDouble());
            case 6: // Double
                return ValueDouble.of(((NBTTagDouble) nbt).getDouble());
            case 7: // Byte Array
                return ValueList.ofList(
                    ValueTypes.INTEGER,
                    Arrays.stream(ArrayUtils.toObject(((NBTTagByteArray) nbt).getByteArray()))
                        .map(ValueInteger::of)
                        .collect(Collectors.toList())
                );
            case 8: // String
                return ValueString.of(((NBTTagString) nbt).getString());
            case 9: // List
                return ValueList.ofAll(
                    StreamSupport.stream(((NBTTagList) nbt).spliterator(), false)
                        .map(NBTValueConverter::mapNBTToValue)
                        .toArray(IValue[]::new)
                );
            case 10: // Compound
                return ValueNbt.of((NBTTagCompound) nbt);
            case 11: // Int Array
                return ValueList.ofList(
                    ValueTypes.INTEGER,
                    Arrays.stream(ArrayUtils.toObject(((NBTTagIntArray) nbt).getIntArray()))
                        .map(ValueInteger::of)
                        .collect(Collectors.toList())
                );
            case 12: // Long Array
                return ValueList.ofList(
                    ValueTypes.LONG,
                    Arrays.stream(ArrayUtils.toObject(((NBTTagLongArray) nbt).data))
                        .map(ValueLong::of)
                        .collect(Collectors.toList())
                );
            default:
                return null;
        }
    }
}
