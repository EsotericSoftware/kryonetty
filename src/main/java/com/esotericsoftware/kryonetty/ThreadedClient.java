package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.KryoNetty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedClient extends Client {

    private ExecutorService executor;

    public ThreadedClient(KryoNetty kryoNetty) {
        super(kryoNetty);
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void connect(String host, int port) {
        try {
            this.executor.submit(() -> super.connect(host, port)).get();
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
