package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.KryoNetty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedServer extends Server {

    private final ExecutorService executor;

    public ThreadedServer(KryoNetty kryoNetty) {
        super(kryoNetty);
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start(int port) {
        try {
            this.executor.submit(() -> super.start(port)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
        this.executor.shutdown();
    }
}
