package com.clientserver2;
import akka.actor.Props;

import akka.actor.AbstractActorWithStash;

public class Server extends AbstractActorWithStash {
    public static Props props() {
        return Props.create(Server.class);
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(SleepMsg.class, this::onSleepMsg)
            .match(TextMsg.class, this::onTextMsg)
            .build();
    }

    public Receive sleeping() {
        return receiveBuilder()
            .match(WakeupMsg.class, this::onWakeupMsg)
            .match(TextMsg.class, msg -> stash())
            .build();
    }
    
    void onSleepMsg(SleepMsg msg) {
        System.out.println("Server going to sleep");
        getContext().become(sleeping());
    }

    void onWakeupMsg(WakeupMsg msg) {
        System.out.println("Server waking up");
        getContext().unbecome();
        unstashAll();
    }

    void onTextMsg(TextMsg msg) {
        getSender().tell(new TextMsg(msg.text), getSelf());
    }

}

