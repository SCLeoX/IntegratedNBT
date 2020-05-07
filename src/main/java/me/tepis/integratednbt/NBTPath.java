package me.tepis.integratednbt;

import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class NBTPath {
    private interface Segment {
        String getDisplayText();

        String getCompactDisplayText();

        NBTBase access(NBTBase parent);
    }

    private static class KeySegment implements Segment {
        private final String key;

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
        public NBTBase access(NBTBase parent) {
            if (parent instanceof NBTTagCompound) {
                return ((NBTTagCompound) parent).getTag(this.key);
            } else {
                return null;
            }
        }
    }

    private static class IndexSegment implements Segment {
        private final int index;
        private final String displayText;

        private IndexSegment(int index) {
            this.index = index;
            this.displayText = "§7#§r" + this.index;
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
            return this.displayText;
        }

        @Override
        public String getCompactDisplayText() {
            return "[" + this.index + "]";
        }

        @Override
        public NBTBase access(NBTBase parent) {
            if (parent instanceof NBTTagList) {
                NBTBase base = ((NBTTagList) parent).get(this.index);
                if (base.getId() == 0 /* TagEnd */) {
                    return null;
                }
                return base;
            } else {
                return null;
            }
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

    public static Optional<NBTPath> fromNBT(NBTBase nbtBase) {
        try {
            if (nbtBase instanceof NBTTagCompound) {
                nbtBase = ((NBTTagCompound) nbtBase).getTag(KEY_PATH);
            }
            assert nbtBase instanceof NBTTagList;
            NBTTagList list = (NBTTagList) nbtBase;
            assert list.getTagType() != 10; /* Compound */
            assert list.tagCount() <= MAX_EXTRACTION_DEPTH;
            ArrayList<Segment> segments = new ArrayList<>(list.tagCount());
            for (NBTBase item : list) {
                NBTTagCompound compound = (NBTTagCompound) item;
                String type = compound.getString(KEY_TYPE);
                assert !type.isEmpty();
                if (type.equals(TYPE_KEY)) {
                    assert compound.hasKey(KEY_KEY);
                    String key = compound.getString(KEY_KEY);
                    assert !key.isEmpty();
                    segments.add(new KeySegment(key));
                } else {
                    assert type.equals(TYPE_INDEX);
                    assert compound.hasKey(KEY_INDEX);
                    int index = compound.getInteger(KEY_INDEX);
                    assert index >= 0;
                    segments.add(new IndexSegment(index));
                }
            }
            return Optional.of(new NBTPath(segments));
        } catch (Exception ex) {
            IntegratedNBT.getLogger().error("Failed to decode NBT for ExtractionPath.", ex);
            return Optional.empty();
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

    public NBTTagCompound toNBTCompound() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag(KEY_PATH, this.toNBT());
        return compound;
    }

    public NBTTagList toNBT() {
        NBTTagList list = new NBTTagList();
        for (Segment segment : this.segments) {
            if (segment instanceof KeySegment) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString(KEY_TYPE, TYPE_KEY);
                tag.setString(KEY_KEY, ((KeySegment) segment).key);
                list.appendTag(tag);
            } else {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString(KEY_TYPE, TYPE_INDEX);
                tag.setInteger(KEY_INDEX, ((IndexSegment) segment).index);
                list.appendTag(tag);
            }
        }
        return list;
    }

    public NBTBase extract(NBTBase source) {
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
            return I18n.format("integratednbt:nbt_extractor.root") + " §7➡§r " +
                this.segments.stream()
                    .map(Segment::getDisplayText)
                    .collect(Collectors.joining(" §7➡§r "));
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
}
