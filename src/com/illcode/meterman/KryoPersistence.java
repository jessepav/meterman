package com.illcode.meterman;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import de.javakaffee.kryoserializers.BitSetSerializer;
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
        kryo.register(BitSet.class, new BitSetSerializer());
        kryo.register(Pattern.class, new RegexSerializer());
        kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
        kryo.register(TextBundle.class, new TextBundleSerializer());
        kryo.setReferences(true);  // properly serialize multiple references and cyclic graphs
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

    private static class TextBundleSerializer extends Serializer<TextBundle> {
        public TextBundle copy(Kryo kryo, TextBundle original) {
            return new TextBundle(new HashMap<>(original.getPassageMap()),
                                  new HashMap<>(original.getSubMap()),
                                  original.getParent());
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
}
