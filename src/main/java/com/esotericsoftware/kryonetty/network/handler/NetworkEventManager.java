package com.esotericsoftware.kryonetty.network.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;

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
        for(NetworkListener networkListener : new ArrayList<>(this.eventList)) {
            for(Method method : networkListener.getClass().getMethods()) {
                if(method.isAnnotationPresent(NetworkHandler.class) && method.getAnnotation(NetworkHandler.class) != null) {
                    Class<?>[] methodParameter = method.getParameterTypes();
                    if(methodParameter.length == 1 && networkEvent.getClass().getSimpleName().equals(methodParameter[0].getSimpleName())) {
                        try {
                            method.setAccessible(true);
                            method.invoke(networkListener, networkEvent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
