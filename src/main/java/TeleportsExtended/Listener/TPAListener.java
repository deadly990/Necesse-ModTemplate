package TeleportsExtended.Listener;

import TeleportsExtended.Events.TPARequestEvent;
import TeleportsExtended.Events.TPAResponseEvent;
import com.google.common.cache.*;
import com.google.common.collect.MapMaker;
import necesse.engine.GameEventListener;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class TPAListener {
    private TPARequestListener requestListener;
    private TPAResponseListener responseListener;



    public TPAListener() {
        this.requestListener = new TPARequestListener(this);
        this.responseListener = new TPAResponseListener(this);
    }
    private static RemovalListener<String, TPARequestEvent> listener = notification -> {
        if (notification.getCause() == RemovalCause.EXPIRED) {
            notification.getValue().expire();
        }
    };
    private Cache<String, TPARequestEvent> requests = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).removalListener(listener).build();

    private void onRequest(TPARequestEvent request) {
        if (requests.getIfPresent(request.target.getName()) == null) {
            requests.put(request.target.getName(), request);
            return;
        }
        request.preventDefault();
    }

    private void onResponse(TPAResponseEvent response) {
        TPARequestEvent request = requests.getIfPresent(response.teleportTarget.getName());
        if(request == null) {
            response.preventDefault();
            return;
        }
        request.execute();
        requests.invalidate(response.teleportTarget.getName());
    }

    public GameEventListener<TPARequestEvent> getRequestListener() {
        return requestListener;
    }

    public GameEventListener<TPAResponseEvent> getResponseListener() {
        return responseListener;
    }


    private class TPARequestListener extends GameEventListener<TPARequestEvent> {
        private final TPAListener listener;
        public TPARequestListener(TPAListener listener) {
            super();
            this.listener = listener;
        }
        @Override
        public void onEvent(TPARequestEvent var1) {
            listener.onRequest(var1);

        }
    }

    private class TPAResponseListener extends GameEventListener<TPAResponseEvent> {
        private final TPAListener listener;
        public TPAResponseListener(TPAListener listener) {
            super();
            this.listener = listener;
        }
        @Override
        public void onEvent(TPAResponseEvent var1) {
            listener.onResponse(var1);
        }
    }
}
