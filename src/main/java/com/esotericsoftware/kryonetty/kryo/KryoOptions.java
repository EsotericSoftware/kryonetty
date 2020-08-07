package com.esotericsoftware.kryonetty.kryo;

import java.util.ArrayList;
import java.util.Arrays;

public class KryoOptions {

    ArrayList<Class> classesToRegister;
    int inputBufferSize;
    int outputBufferSize;
    int maxOutputBufferSize;

    public KryoOptions() {
        this.classesToRegister = new ArrayList<>();
        this.inputBufferSize = -1;
        this.outputBufferSize = -1;
        this.maxOutputBufferSize = -1;
    }

    public KryoOptions register(Class clazz) {
        this.classesToRegister.add(clazz);
        return this;
    }

    public KryoOptions register(Class... clazz) {
        if(clazz.length != 0)
            this.classesToRegister.addAll(Arrays.asList(clazz));
        return this;
    }

    public KryoOptions inputSize(int inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
        return this;
    }

    public KryoOptions outputSize(int outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
        return this;
    }

    public KryoOptions maxOutputSize(int maxOutputBufferSize) {
        this.maxOutputBufferSize = maxOutputBufferSize;
        return this;
    }

    protected ArrayList<Class> getClassesToRegister() {
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
