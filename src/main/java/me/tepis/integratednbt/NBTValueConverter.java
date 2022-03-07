package me.tepis.integratednbt;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
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

public abstract class NBTValueConverter {
    public static IValueType<? extends IValue> mapNBTToValueType(Tag nbt) {
        return mapNBTIDToValueType(nbt.getId());
    }

    public static IValueType<?> mapNBTIDToValueType(int nbtId) {
        switch (nbtId) {
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
                throw new RuntimeException("Unexpected NBT id:" + nbtId);
        }
    }

    public static String getDefaultValueDisplayText(int nbtId) {
        String formatCode = getDefaultValue(nbtId).getType().getDisplayColorFormat().toString();
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
                return ValueNbt.of(new CompoundTag());
        }
    }

    public static IValue mapNBTToValue(Tag nbt) {
        switch (nbt.getId()) {
            case 1: // Byte
                return ValueInteger.of(((ByteTag) nbt).getAsInt());
            case 2: // Short
                return ValueInteger.of(((ShortTag) nbt).getAsInt());
            case 3: // Int
                return ValueInteger.of(((IntTag) nbt).getAsInt());
            case 4: // Long
                return ValueLong.of(((LongTag) nbt).getAsLong());
            case 5: // Float
                return ValueDouble.of(((FloatTag) nbt).getAsDouble());
            case 6: // Double
                return ValueDouble.of(((DoubleTag) nbt).getAsDouble());
            case 7: // Byte Array
                return ValueList.ofList(
                    ValueTypes.INTEGER,
                    Arrays.stream(ArrayUtils.toObject(((ByteArrayTag) nbt).getAsByteArray()))
                        .map(ValueInteger::of)
                        .collect(Collectors.toList())
                );
            case 8: // String
                return ValueString.of(nbt.getAsString());
            case 9: // List
                return ValueList.ofAll(
                    ((ListTag) nbt).stream()
                        .map(NBTValueConverter::mapNBTToValue)
                        .toArray(IValue[]::new)
                );
            case 10: // Compound
                return ValueNbt.of(nbt);
            case 11: // Int Array
                return ValueList.ofList(
                    ValueTypes.INTEGER,
                    Arrays.stream(ArrayUtils.toObject(((IntArrayTag) nbt).getAsIntArray()))
                        .map(ValueInteger::of)
                        .collect(Collectors.toList())
                );
            case 12: // Long Array
                return ValueList.ofList(
                    ValueTypes.LONG,
                    Arrays.stream(ArrayUtils.toObject(((LongArrayTag) nbt).getAsLongArray()))
                        .map(ValueLong::of)
                        .collect(Collectors.toList())
                );
            default:
                return null;
        }
    }
}
