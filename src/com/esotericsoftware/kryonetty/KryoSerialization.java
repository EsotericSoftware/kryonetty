package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

import java.util.Arrays;

public class KryoHolder {

    private static final int DEFAULT_INPUT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 2048;

    private final Pool<Kryo> kryoPool;
    private final Pool<Input> inputPool;
    private final Pool<Output> outputPool;

    public KryoHolder(int inputBufferSize, int outputBufferSize, Class... classes) {
        kryoPool = new Pool<Kryo>(true, true) {
            @Override
            protected Kryo create() {
                Kryo kryo = new Kryo();

                kryo.setRegistrationRequired(true);
                kryo.setReferences(false);

                if (classes.length != 0)
                    Arrays.stream(classes).forEach(kryo::register);

                return kryo;
            }
        };
        inputPool = new Pool<Input>(true, true) {
            @Override
            protected Input create() {
                return new Input(inputBufferSize == -1 ? DEFAULT_INPUT_BUFFER_SIZE : inputBufferSize);
            }
        };
        outputPool = new Pool<Output>(true, true) {
            @Override
            protected Output create() {
                return new Output(outputBufferSize == -1 ? DEFAULT_OUTPUT_BUFFER_SIZE : outputBufferSize, -1);
            }
        };
    }

    public Kryo getKryo() {
        return kryoPool.obtain();
    }

    public void freeKryo(Kryo kryo) {
        kryoPool.free(kryo);
    }

    public Input getInput() {
        return inputPool.obtain();
    }

    public void freeInput(Input input) {
        inputPool.free(input);
    }

    public Output getOutput() {
        return outputPool.obtain();
    }

    public void freeOutput(Output output) {
        outputPool.free(output);
    }
}
