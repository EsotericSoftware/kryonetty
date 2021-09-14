package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryo.Kryo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class KryoNetty {

    Consumer<Kryo> initializationConsumer;

    boolean useLogging = false;
    boolean useExecution = false;
    int executionThreadSize = 1;

    Map<Integer, Class<?>> classesToRegister = new ConcurrentHashMap<>();
    int inputBufferSize = -1;
    int outputBufferSize = -1;
    int maxOutputBufferSize = -1;

    public KryoNetty() {
    }

    public KryoNetty initialization(Consumer<Kryo> consumer) {
        initializationConsumer = consumer;
        return this;
    }

    public KryoNetty useLogging() {
        useLogging = true;
        return this;
    }

    public KryoNetty useExecution() {
        useExecution = true;
        return this;
    }

    public KryoNetty threadSize(int size) {
        executionThreadSize = size;
        return this;
    }

    public KryoNetty register(int index, Class<?> clazz) {
        classesToRegister.put(index, clazz);
        return this;
    }

    public KryoNetty register(Class<?> clazz) {
        classesToRegister.put(this.classesToRegister.size() + 1, clazz);
        return this;
    }

    public KryoNetty register(Class<?>... clazzez) {
        if(clazzez.length != 0) {
            for(Class<?> clazz : clazzez) {
                classesToRegister.put(classesToRegister.size() + 1, clazz);
            }
        }
        return this;
    }

    public KryoNetty inputSize(int newInputSize) {
        inputBufferSize = newInputSize;
        return this;
    }

    public KryoNetty outputSize(int newOutputSize) {
        outputBufferSize = newOutputSize;
        return this;
    }

    public KryoNetty maxOutputSize(int newMaxOutputSize) {
        maxOutputBufferSize = newMaxOutputSize;
        return this;
    }

    protected Consumer<Kryo> getInitializationConsumer() {
        return initializationConsumer;
    }

    public boolean isUseLogging() {
        return useLogging;
    }

    public boolean isUseExecution() {
        return useExecution;
    }

    public int getExecutionThreadSize() {
        return executionThreadSize;
    }

    protected Map<Integer, Class<?>> getClassesToRegister() {
        return classesToRegister;
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public int getOutputBufferSize() {
        return outputBufferSize;
    }

    protected int getMaxOutputBufferSize() {
        return maxOutputBufferSize;
    }
}
