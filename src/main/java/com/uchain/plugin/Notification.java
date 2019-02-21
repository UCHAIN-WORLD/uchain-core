package com.uchain.plugin;

import akka.actor.ActorRef;
import com.uchain.plugin.notifymessage.NotifyMessage;

import java.util.ArrayList;
import java.util.List;

public class Notification {

    private List<ActorRef> listeners = new ArrayList<>();

    public void register(ActorRef actorRef) {
        listeners.add(actorRef);
    }

    public void send(NotifyMessage notifyMessage, ActorRef sender) {
        listeners.forEach(actorRef -> actorRef.tell(notifyMessage, sender));
    }
}
