package com.illcode.meterman;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.InputStream;
import java.io.OutputStream;

public final class KryoPersistence implements Persistence
{
    private Kryo kryo;

    public void init() {
        kryo = new Kryo();
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
}
