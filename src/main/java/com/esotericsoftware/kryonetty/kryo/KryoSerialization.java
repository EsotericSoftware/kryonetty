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

    /*
    Class ID's:
     -1 & -2 are reserved by Kryo
    0 - 8 are reserved for primitives
    10 - 100 are reserved by java objects
    100- ??? are for userspace
     */
    public KryoSerialization(KryoNetty kryoNetty) {
        kryoPool = new Pool<Kryo>(true, true) {
            @Override
            protected Kryo create() {
                Kryo kryo = new Kryo();

                kryo.setRegistrationRequired(true);
                kryo.setReferences(true);
                kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());

                SerializerFactory.CompatibleFieldSerializerFactory factory = new SerializerFactory.CompatibleFieldSerializerFactory();

                // FieldSerializerConfig
                factory.getConfig().setFieldsCanBeNull(true);
                factory.getConfig().setFieldsAsAccessible(true);
                factory.getConfig().setIgnoreSyntheticFields(false);
                factory.getConfig().setFixedFieldTypes(false);
                factory.getConfig().setCopyTransient(true);
                factory.getConfig().setSerializeTransient(false);
                factory.getConfig().setVariableLengthEncoding(true);
                factory.getConfig().setExtendedFieldNames(true);

                // CompatibleFieldSerializerConfig
                factory.getConfig().setReadUnknownFieldData(false);
                factory.getConfig().setChunkedEncoding(false);

                // Adding Factory as Serializer
                kryo.setDefaultSerializer(factory);



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
                kryo.register(HashMap.class, 10);
                kryo.register(ArrayList.class, 11);
                kryo.register(HashSet.class, 12);
                kryo.register(byte[].class, 13);
                kryo.register(char[].class, 14);
                kryo.register(short[].class, 15);
                kryo.register(int[].class, 16);
                kryo.register(long[].class, 17);
                kryo.register(float[].class, 18);
                kryo.register(double[].class, 19);
                kryo.register(boolean[].class, 20);
                kryo.register(String[].class, 21);
                kryo.register(Object[].class, 22);
                kryo.register(BigInteger.class, 23);
                kryo.register(BigDecimal.class, 24);
                kryo.register(Class.class, 25);
                kryo.register(Date.class, 26);
                kryo.register(StringBuffer.class, 27);
                kryo.register(StringBuilder.class, 28);
                kryo.register(Collections.EMPTY_LIST.getClass(), 29);
                kryo.register(Collections.EMPTY_MAP.getClass(), 30);
                kryo.register(Collections.EMPTY_SET.getClass(), 31);
                kryo.register(Collections.singleton(null).getClass(), 32);
                kryo.register(Collections.singletonList(null).getClass(), 33);
                kryo.register(Collections.singletonMap(null, null).getClass(), 34);
                kryo.register(TreeSet.class, 35);
                kryo.register(Collection.class, 36);
                kryo.register(TreeMap.class, 37);
                kryo.register(Map.class, 38);
                kryo.register(TimeZone.class, 39);
                kryo.register(Calendar.class, 40);
                kryo.register(Locale.class, 41);
                kryo.register(Charset.class, 42);
                kryo.register(URL.class, 43);
                kryo.register(Arrays.asList().getClass(), 44);
                kryo.register(PriorityQueue.class, 45);
                kryo.register(BitSet.class, 46);

                if (!kryoNetty.getClassesToRegister().isEmpty())
                    kryoNetty.getClassesToRegister().forEach((key, value) -> kryo.register(value, (key + 100)));

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
