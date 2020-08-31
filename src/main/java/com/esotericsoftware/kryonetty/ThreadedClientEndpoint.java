package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.KryoNetty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedClientEndpoint extends ClientEndpoint {

    private final ExecutorService executorService;

    public ThreadedClientEndpoint(KryoNetty kryoNetty) {
        super(kryoNetty);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void connect(String host, int port) {
        try {
            this.executorService.submit(() -> super.connect(host, port)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        super.close();
        this.executorService.shutdown();
    }
}
