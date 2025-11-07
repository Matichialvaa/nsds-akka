package com.clientserver2;

import akka.actor.ActorRef;

public final class StartClientMsg {
    // pass server reference to client
    public final ActorRef server;

    public StartClientMsg(ActorRef server) {
        this.server = server;
    }
}
