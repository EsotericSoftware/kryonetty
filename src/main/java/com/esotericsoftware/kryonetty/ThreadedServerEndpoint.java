package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.KryoNetty;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedServerEndpoint extends ServerEndpoint {

    private final ExecutorService executorService;

    public ThreadedServerEndpoint(KryoNetty kryoNetty) {
        super(kryoNetty);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start(int port) {
        try {
            this.executorService.submit(() -> super.start(port)).get();
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
