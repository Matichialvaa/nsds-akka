package com.clientserver2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class App {

    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create("System");
    final ActorRef server = system.actorOf(Server.props(), "Server");
    final ActorRef client = system.actorOf(Client.props(), "Client");

    server.tell(new SleepMsg(), ActorRef.noSender());
    client.tell(new StartClientMsg(server), ActorRef.noSender());

        Thread.sleep(800);
        system.terminate();
    }

}
