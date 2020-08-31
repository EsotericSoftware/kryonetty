package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.util.Pool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    * Class ID's reserved:
    *  -1 & -2  -> Kryo
    *   0 - 8   -> java-primitives
    *  10 - 100 -> standard-java-objects
    * 100++     -> user-space
     */
    public KryoSerialization(KryoNetty kryoNetty) {

        // Initialize Kryo-Pool
        kryoPool = new Pool<Kryo>(true, true) {
            @Override
            protected Kryo create() {
                // Create new Kryo instance
                Kryo kryo = new Kryo();

                // Configure Kryo-instance

                // Registration of classes are required to avoid wrong class-decoding
                kryo.setRegistrationRequired(true);

                // Refereneces are required for object-graph
                kryo.setReferences(true);
                kryo.addDefaultSerializer(Throwable.class, new JavaSerializer());

                // Use CompatibleSerializer for back- and upward-compatibility
                SerializerFactory.CompatibleFieldSerializerFactory factory = new SerializerFactory.CompatibleFieldSerializerFactory();

                // FieldSerializerConfig
                factory.getConfig().setFieldsCanBeNull(true);
                factory.getConfig().setFieldsAsAccessible(true);
                factory.getConfig().setIgnoreSyntheticFields(true);
                factory.getConfig().setFixedFieldTypes(false);
                factory.getConfig().setCopyTransient(true);
                factory.getConfig().setSerializeTransient(false);
                factory.getConfig().setVariableLengthEncoding(true);
                factory.getConfig().setExtendedFieldNames(true);

                // CompatibleFieldSerializerConfig
                factory.getConfig().setReadUnknownFieldData(false);
                factory.getConfig().setChunkedEncoding(true);

                // Adding Factory as Serializer
                kryo.setDefaultSerializer(factory);

                // Register standard-java-objects
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

                // Register KryoNetty Classes
                if (!kryoNetty.getClassesToRegister().isEmpty())
                    kryoNetty.getClassesToRegister().forEach((key, value) -> kryo.register(value, (key + 100)));

                return kryo;
            }
        };

        // Initialize Input-Pool
        inputPool = new Pool<Input>(true, true) {
            @Override
            protected Input create() {
                // Create new Input instance
                return new Input(kryoNetty.getInputBufferSize() == -1 ? DEFAULT_INPUT_BUFFER_SIZE : kryoNetty.getInputBufferSize());
            }
        };

        // Initialize Output-Pool
        outputPool = new Pool<Output>(true, true) {
            @Override
            protected Output create() {
                // Create new Output instance
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

    public <T> byte[] encodeToBytes(T object) {
        Kryo kryo = getKryo();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = getOutput();
        output.setOutputStream(outputStream);
        kryo.writeClassAndObject(output, object);
        output.flush();
        output.close();
        freeKryo(kryo);
        freeOutput(output);
        return outputStream.toByteArray();
    }

    public <T> T decodeFromBytes(byte[] bytes) {
        Kryo kryo = getKryo();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Input input = getInput();
        input.setInputStream(inputStream);
        T object = (T) kryo.readClassAndObject(input);
        input.close();
        freeKryo(kryo);
        freeInput(input);
        return object;
    }
}
