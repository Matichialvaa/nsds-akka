package com.clientserver;

import java.util.HashMap;
import java.util.Map;
import akka.actor.Props;
import akka.actor.AbstractActor;

public class ContactServer extends AbstractActor {
    // address -> name
    private final Map<String,String> contacts = new HashMap<>();
    
    public static Props props() {
        return Props.create(ContactServer.class);
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(PutMsg.class, this::onPutMsg)
            .match(GetMsg.class, this::onGetMsg)
            .build();
    }

    void onPutMsg(PutMsg msg) {
        if ("Fail!".equals(msg.name)) {
            throw new RuntimeException("Simulated failure for name 'Fail!'");
        }
        contacts.put(msg.name, msg.email);
    }

    void onGetMsg(GetMsg msg) {
        String email = contacts.get(msg.name);
        getSender().tell(new GetResponse(msg.name, java.util.Optional.ofNullable(email)), getSelf());
    }
}