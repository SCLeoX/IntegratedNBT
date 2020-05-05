package me.tepis.integratednbt;

public class ByteMaskMaker {
    private byte mask = 1;

    public byte nextMask() {
        byte mask = this.mask;
        this.mask = (byte) (mask << 1);
        if (mask < 0) {
            throw new RuntimeException("Byte mask overflow.");
        }
        return mask;
    }
}
