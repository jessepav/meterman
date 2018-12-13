package com.illcode.meterman;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.RegexSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Uses Kryo to serialize world state.
 * <p/>
 * Note that if any of the classes referenced in the state changes, then saved
 * data cannot be loaded.
 */
public final class KryoPersistence implements Persistence
{
    private Kryo kryo;

    public void init() {
        kryo = new Kryo();
        // My serializers
        kryo.register(BitSet.class, new BitSetSerializer());
        kryo.register(TextBundle.class, new TextBundleSerializer());
        // Serializers from kryo-serializers
        kryo.register(Pattern.class, new RegexSerializer());
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
        // properly serialize multiple references and cyclic graphs
        kryo.setReferences(true);
    }

    public void dispose() {
        kryo = null;
    }

    public void saveWorldState(WorldState state, OutputStream out) {
        Output output = new Output(out);
        kryo.writeObject(output, state);
        output.flush();
    }

    public WorldState loadWorldState(InputStream in) {
        Input input = new Input(in);
        WorldState state = kryo.readObject(input, WorldState.class);
        return state;
    }

    public WorldState copyWorldState(WorldState ws) {
        return kryo.copy(ws);
    }

    private static class TextBundleSerializer extends Serializer<TextBundle> {
        public TextBundle copy(Kryo kryo, TextBundle original) {
            // Since the only place copy() is used is to implement undo, we want it fast, and can just
            // reuse the original, on the assumption that nothing critical will have changed in one turn.
            return original;
        }

        public void write(Kryo kryo, Output output, TextBundle bundle) {
            kryo.writeClassAndObject(output, bundle.getPassageMap());
            kryo.writeClassAndObject(output, bundle.getSubMap());
            TextBundle parent = bundle.getParent();
            if (parent == Meterman.systemBundle) {
                output.writeBoolean(true);
            } else {
                output.writeBoolean(false);
                kryo.writeObjectOrNull(output, parent, TextBundle.class);
            }
        }

        @SuppressWarnings("unchecked")
        public TextBundle read(Kryo kryo, Input input, Class<TextBundle> type) {
            Map<String,String> passageMap = (Map<String,String>) kryo.readClassAndObject(input);
            Map<String,String> subMap = (Map<String,String>) kryo.readClassAndObject(input);
            TextBundle parent;
            boolean isSystemParent = input.readBoolean();
            if (isSystemParent)
                parent = Meterman.systemBundle;
            else
                parent = kryo.readObjectOrNull(input, TextBundle.class);
            return new TextBundle(passageMap, subMap, parent);
        }
    }

    private static class BitSetSerializer extends Serializer<BitSet> {
        public BitSet copy(Kryo kryo, BitSet original) {
            return BitSet.valueOf(original.toLongArray());
        }

        public void write(Kryo kryo, Output output, BitSet bitSet) {
            long[] longs = bitSet.toLongArray();
            output.writeInt(longs.length, true);
            output.writeLongs(longs);
        }

        public BitSet read(Kryo kryo, Input input, Class<BitSet> type) {
            int len = input.readInt(true);
            return BitSet.valueOf(input.readLongs(len));
        }
    }
}
