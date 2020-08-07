package com.esotericsoftware.kryonetty.kryo;

import java.util.ArrayList;
import java.util.Arrays;

public class KryoNetty {

    boolean useLogging;
    boolean useExecution;
    int executionThreadSize;

    ArrayList<Class<?>> classesToRegister;
    int inputBufferSize;
    int outputBufferSize;
    int maxOutputBufferSize;

    public KryoNetty() {
        this.useLogging = false;
        this.useExecution = false;
        this.executionThreadSize = 0;


        this.classesToRegister = new ArrayList<>();
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
        this.executionThreadSize = 8;
        return this;
    }

    public KryoNetty threadSize(int size) {
        this.executionThreadSize = size;
        return this;
    }

    public KryoNetty register(Class<?> clazz) {
        this.classesToRegister.add(clazz);
        return this;
    }

    public KryoNetty register(Class<?>... clazzez) {
        if(clazzez.length != 0)
            this.classesToRegister.addAll(Arrays.asList(clazzez));
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

    protected ArrayList<Class<?>> getClassesToRegister() {
        return classesToRegister;
    }

    protected int getInputBufferSize() {
        return inputBufferSize;
    }

    protected int getOutputBufferSize() {
        return outputBufferSize;
    }

    protected int getMaxOutputBufferSize() {
        return maxOutputBufferSize;
    }
}
