package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.util.Pool;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class KryoSerialization {

    private static final int DEFAULT_INPUT_BUFFER_SIZE = 2048;
    private static final int DEFAULT_OUTPUT_BUFFER_SIZE = 2048;

    private final Pool<Kryo> kryoPool;
    private final Pool<Input> inputPool;
    private final Pool<Output> outputPool;

    public KryoSerialization(KryoNetty kryoNetty) {
        kryoPool = new Pool<Kryo>(true, true) {
            @Override
            protected Kryo create() {
                Kryo kryo = new Kryo();

                kryo.setRegistrationRequired(true);
                kryo.setReferences(true);
                kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());

                kryo.register(HashMap.class);
                kryo.register(ArrayList.class);
                kryo.register(HashSet.class);
                kryo.register(byte[].class);
                kryo.register(char[].class);
                kryo.register(short[].class);
                kryo.register(int[].class);
                kryo.register(long[].class);
                kryo.register(float[].class);
                kryo.register(double[].class);
                kryo.register(boolean[].class);
                kryo.register(String[].class);
                kryo.register(Object[].class);
                kryo.register(BigInteger.class);
                kryo.register(BigDecimal.class);
                kryo.register(Class.class);
                kryo.register(Date.class);
                kryo.register(StringBuffer.class);
                kryo.register(StringBuilder.class);
                kryo.register(Collections.EMPTY_LIST.getClass());
                kryo.register(Collections.EMPTY_MAP.getClass());
                kryo.register(Collections.EMPTY_SET.getClass());
                kryo.register(Collections.singleton(null).getClass());
                kryo.register(Collections.singletonList(null).getClass());
                kryo.register(Collections.singletonMap(null, null).getClass());
                kryo.register(TreeSet.class);
                kryo.register(Collection.class);
                kryo.register(TreeMap.class);
                kryo.register(Map.class);
                kryo.register(TimeZone.class);
                kryo.register(Calendar.class);
                kryo.register(Locale.class);
                kryo.register(Charset.class);
                kryo.register(URL.class);
                kryo.register(Arrays.asList().getClass());
                kryo.register(PriorityQueue.class);
                kryo.register(BitSet.class);

                if (!kryoNetty.getClassesToRegister().isEmpty())
                    kryoNetty.getClassesToRegister().forEach(clazz -> {
                        System.out.println(clazz.getSimpleName());
                        kryo.register(clazz);
                    });

                return kryo;
            }
        };
        inputPool = new Pool<Input>(true, true) {
            @Override
            protected Input create() {
                return new Input(kryoNetty.getInputBufferSize() == -1 ? DEFAULT_INPUT_BUFFER_SIZE : kryoNetty.getInputBufferSize());
            }
        };
        outputPool = new Pool<Output>(true, true) {
            @Override
            protected Output create() {
                return new Output(
                        kryoNetty.getOutputBufferSize() == -1 ? DEFAULT_OUTPUT_BUFFER_SIZE : kryoNetty.getOutputBufferSize(),
                        kryoNetty.getMaxOutputBufferSize());
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
