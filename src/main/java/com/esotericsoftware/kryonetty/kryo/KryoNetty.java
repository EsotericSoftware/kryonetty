package com.esotericsoftware.kryonetty.kryo;

import java.util.Arrays;
import java.util.HashMap;

public class KryoNetty {

    boolean useLogging;
    boolean useExecution;
    int executionThreadSize;

    HashMap<Integer, Class<?>> classesToRegister;
    int inputBufferSize;
    int outputBufferSize;
    int maxOutputBufferSize;

    public KryoNetty() {
        this.useLogging = false;
        this.useExecution = false;
        this.executionThreadSize = 1;

        this.classesToRegister = new HashMap<>();
        this.inputBufferSize = -1;
        this.outputBufferSize = -1;
        this.maxOutputBufferSize = -1;
    }

    public KryoNetty useLogging() {
        this.useLogging = true;
        return this;
    }

    public KryoNetty useExecution() {
        this.useExecution = true;
        return this;
    }

    public KryoNetty threadSize(int size) {
        this.executionThreadSize = size;
        return this;
    }

    public KryoNetty register(int index, Class<?> clazz) {
        this.classesToRegister.put(index, clazz);
        return this;
    }

    public KryoNetty register(Class<?> clazz) {
        this.classesToRegister.put(this.classesToRegister.size() + 1, clazz);
        return this;
    }

    public KryoNetty register(Class<?>... clazzez) {
        if(clazzez.length != 0)
            Arrays.stream(clazzez).forEach(clazz -> this.classesToRegister.put(this.classesToRegister.size() + 1, clazz));
        return this;
    }

    public KryoNetty inputSize(int inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
        return this;
    }

    public KryoNetty outputSize(int outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
        return this;
    }

    public KryoNetty maxOutputSize(int maxOutputBufferSize) {
        this.maxOutputBufferSize = maxOutputBufferSize;
        return this;
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

    protected HashMap<Integer, Class<?>> getClassesToRegister() {
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
