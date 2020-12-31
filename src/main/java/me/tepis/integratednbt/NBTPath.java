package me.tepis.integratednbt;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NBTPath {
    private interface Segment {
        String getDisplayText();

        String getCompactDisplayText();

        INBT access(INBT parent);

        void buildCyclopsNBTPath(StringBuilder stringBuilder);
    }

    private static class KeySegment implements Segment {
        private final String key;
        private static final Pattern NON_SPECIAL = Pattern.compile("^[a-zA-Z_0-9]+$");

        public KeySegment(String key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }

            KeySegment that = (KeySegment) o;

            return this.key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return this.key.hashCode();
        }

        @Override
        public String getDisplayText() {
            return this.key;
        }

        @Override
        public String getCompactDisplayText() {
            return "." + this.key;
        }

        @Override
        public INBT access(INBT parent) {
            if (parent instanceof CompoundNBT) {
                return ((CompoundNBT) parent).get(this.key);
            } else {
                return null;
            }
        }

        @Override
        public void buildCyclopsNBTPath(StringBuilder stringBuilder) {
            // .length is reserved in Cyclops NBT Path
            if (NON_SPECIAL.matcher(this.key).matches() && !this.key.equals("length")) {
                stringBuilder.append('.').append(this.key);
            } else {
                // Cyclops NBT Path currently does not support escaping
                stringBuilder.append("[\"").append(
                    StringUtils.replace(
                        StringUtils.replace(this.key, "\\", "\\\\"),
                        "\"",
                        "\\\""
                    )
                ).append("\"]");
            }
        }
    }

    private static class IndexSegment implements Segment {
        private final int index;

        private IndexSegment(int index) {
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }

            IndexSegment that = (IndexSegment) o;

            return this.index == that.index;
        }

        @Override
        public int hashCode() {
            return this.index;
        }

        @Override
        public String getDisplayText() {
            return I18n.format(
                "integratednbt:nbt_extractor.index",
                String.valueOf(index)
            );
        }

        @Override
        public String getCompactDisplayText() {
            return "[" + this.index + "]";
        }

        @Override
        public INBT access(INBT parent) {
            if (parent instanceof ListNBT) {
                INBT base = ((ListNBT) parent).get(this.index);
                if (base.getId() == 0 /* TagEnd */) {
                    return null;
                }
                return base;
            } else {
                return null;
            }
        }

        @Override
        public void buildCyclopsNBTPath(StringBuilder stringBuilder) {
            stringBuilder.append("[").append(this.index).append("]");
        }
    }

    private static final int MAX_EXTRACTION_DEPTH = 128;
    private static final String KEY_PATH = "path";
    private static final String KEY_TYPE = "type";
    private static final String TYPE_KEY = "key";
    private static final String KEY_KEY = "key";
    private static final String TYPE_INDEX = "index";
    private static final String KEY_INDEX = "index";
    // Although we are doing a lot of push and pop, since the max number of elements is limited,
    // array lists should be better than linked lists.
    private final ArrayList<Segment> segments;

    public NBTPath(ArrayList<Segment> segments) {
        this.segments = segments;
    }

    public NBTPath() {
        this.segments = new ArrayList<>();
    }

    public static Optional<NBTPath> fromNBT(INBT nbt) {
        try {
            if (nbt instanceof CompoundNBT) {
                nbt = ((CompoundNBT) nbt).get(KEY_PATH);
            }
            myAssert(nbt instanceof ListNBT);
            ListNBT list = (ListNBT) nbt;
            myAssert(list.getTagType() == 10 || list.size() == 0); /* Compound */
            myAssert(list.size() <= MAX_EXTRACTION_DEPTH);
            ArrayList<Segment> segments = new ArrayList<>(list.size());
            for (INBT item : list) {
                CompoundNBT compound = (CompoundNBT) item;
                String type = compound.getString(KEY_TYPE);
                myAssert(!type.isEmpty());
                if (type.equals(TYPE_KEY)) {
                    myAssert(compound.contains(KEY_KEY));
                    String key = compound.getString(KEY_KEY);
                    myAssert(!key.isEmpty());
                    segments.add(new KeySegment(key));
                } else {
                    myAssert(type.equals(TYPE_INDEX));
                    myAssert(compound.contains(KEY_INDEX));
                    int index = compound.getInt(KEY_INDEX);
                    myAssert(index >= 0);
                    segments.add(new IndexSegment(index));
                }
            }
            return Optional.of(new NBTPath(segments));
        } catch (Exception ex) {
            IntegratedNBT.LOGGER.error("Failed to decode NBT for ExtractionPath.", ex);
            return Optional.empty();
        }
    }

    private static void myAssert(boolean value) {
        // Apparently Java assert doesn't work :(
        if (!value) {
            throw new RuntimeException("Assertion failed. D:");
        }
    }

    public void pushKey(String key) {
        this.segments.add(new KeySegment(key));
    }

    public void pushIndex(int index) {
        this.segments.add(new IndexSegment(index));
    }

    public void pop() {
        this.segments.remove(this.segments.size() - 1);
    }

    @SuppressWarnings("unchecked")
    public NBTPath copy() {
        return new NBTPath((ArrayList<Segment>) this.segments.clone());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        NBTPath that = (NBTPath) o;

        return this.segments.equals(that.segments);
    }

    @Override
    public int hashCode() {
        return this.segments.hashCode();
    }

    public CompoundNBT toNBTCompound() {
        CompoundNBT compound = new CompoundNBT();
        compound.put(KEY_PATH, this.toNBT());
        return compound;
    }

    public ListNBT toNBT() {
        ListNBT list = new ListNBT();
        for (Segment segment : this.segments) {
            if (segment instanceof KeySegment) {
                CompoundNBT tag = new CompoundNBT();
                tag.putString(KEY_TYPE, TYPE_KEY);
                tag.putString(KEY_KEY, ((KeySegment) segment).key);
                list.add(tag);
            } else {
                CompoundNBT tag = new CompoundNBT();
                tag.putString(KEY_TYPE, TYPE_INDEX);
                tag.putInt(KEY_INDEX, ((IndexSegment) segment).index);
                list.add(tag);
            }
        }
        return list;
    }

    public INBT extract(INBT source) {
        for (Segment segment : this.segments) {
            if (source == null) {
                return null;
            }
            source = segment.access(source);
        }
        return source;

    }

    public String getDisplayText() {
        if (this.segments.isEmpty()) {
            return I18n.format("integratednbt:nbt_extractor.root");
        } else {
            String arrow = I18n.format("integratednbt:nbt_extractor.arrow");
            return I18n.format("integratednbt:nbt_extractor.root") + arrow +
                this.segments.stream()
                    .map(Segment::getDisplayText)
                    .collect(Collectors.joining(arrow));
        }
    }

    public String getCompactDisplayText() {
        if (this.segments.isEmpty()) {
            return "id";
        } else {
            return this.segments.stream()
                .map(Segment::getCompactDisplayText)
                .collect(Collectors.joining());
        }
    }

    public int getDepth() {
        return this.segments.size();
    }

    public String getCyclopsNBTPath() {
        StringBuilder stringBuilder = new StringBuilder().append('$');
        for (Segment segment : this.segments) {
            segment.buildCyclopsNBTPath(stringBuilder);
        }
        return stringBuilder.toString();
    }
}
