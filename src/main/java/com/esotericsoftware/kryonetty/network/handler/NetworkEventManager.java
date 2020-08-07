package com.esotericsoftware.kryonetty.network.handler;

import java.util.ArrayList;
import java.util.Arrays;

public class NetworkEventManager {

    private final ArrayList<NetworkListener> eventList;

    public NetworkEventManager() {
        this.eventList = new ArrayList<>();
    }

    public void register(NetworkListener networkListener) {
        if (!this.eventList.contains(networkListener))
            this.eventList.add(networkListener);
    }

    public void unregisterAll() {
        this.eventList.clear();
    }

    public void callEvent(NetworkEvent networkEvent) {
        this.eventList.forEach(networkListener ->
                Arrays.stream(networkListener.getClass().getMethods())
                        .filter(method -> method.isAnnotationPresent(NetworkHandler.class))
                        .filter(method -> method.getAnnotation(NetworkHandler.class) != null).forEach(method -> {
                    Class<?>[] methodParameter = method.getParameterTypes();
                    if(methodParameter.length == 1 && networkEvent.getClass().getSimpleName().equals(methodParameter[0].getSimpleName())) {
                        try {
                            method.setAccessible(true);
                            method.invoke(networkListener, networkEvent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }));
    }
}
