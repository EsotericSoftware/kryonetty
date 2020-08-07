package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

public class KryoHolder {

    private static final int DEFAULT_INPUT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 2048;

    private final Endpoint endpoint;

    private final Pool<Kryo> kryoPool;
    private final Pool<Input> inputPool;
    private final Pool<Output> outputPool;

    public KryoHolder(Endpoint endpoint) {
        this.endpoint = endpoint;
        kryoPool = new Pool<Kryo>(true, true) {
            @Override
            protected Kryo create() {
                Kryo kryo = new Kryo();

                kryo.setRegistrationRequired(true);
                kryo.setReferences(false);

                if (!endpoint.kryoOptions().getClassesToRegister().isEmpty())
                    endpoint.kryoOptions().getClassesToRegister().forEach(kryo::register);

                return kryo;
            }
        };
        inputPool = new Pool<Input>(true, true) {
            @Override
            protected Input create() {
                return new Input(endpoint.kryoOptions().getInputBufferSize() == -1 ? DEFAULT_INPUT_BUFFER_SIZE : endpoint.kryoOptions().getInputBufferSize());
            }
        };
        outputPool = new Pool<Output>(true, true) {
            @Override
            protected Output create() {
                return new Output(endpoint.kryoOptions().getOutputBufferSize() == -1 ? DEFAULT_OUTPUT_BUFFER_SIZE : endpoint.kryoOptions().getOutputBufferSize(), endpoint.kryoOptions().getMaxOutputBufferSize());
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
