package com.clientserver2;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Client extends AbstractActor {
    public static Props props() { return Props.create(Client.class); }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(StartClientMsg.class, this::onStart)
            .match(TextMsg.class, this::onText)
            .build();
    }

    void onStart(StartClientMsg msg) {
        ActorRef server = msg.server;
        server.tell(new TextMsg("m1"), getSelf());
        server.tell(new TextMsg("m2"), getSelf());
        server.tell(new WakeupMsg(), getSelf());
        server.tell(new TextMsg("m3"), getSelf());
    }

    void onText(TextMsg msg) {
        System.out.println("ContactClient received: " + msg.text);
    }

}
