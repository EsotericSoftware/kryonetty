package com.esotericsoftware.kryonetty.kryo;

public class EndpointOptions {

    boolean useLogging;
    boolean useExecution;
    int executionThreadSize;

    public EndpointOptions() {
        this.useLogging = false;
        this.useExecution = false;
        this.executionThreadSize = 0;
    }
    public EndpointOptions useLogging() {
        this.useLogging = true;
        return this;
    }

    public EndpointOptions useExecution() {
        this.useExecution = true;
        this.executionThreadSize = 8;
        return this;
    }

    public EndpointOptions threadSize(int size) {
        this.executionThreadSize = size;
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
}
