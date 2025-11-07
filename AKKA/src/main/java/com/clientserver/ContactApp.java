package com.clientserver;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;


public class ContactApp {

    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create("contacts-system");
        final ActorRef server = system.actorOf(ContactServer.props(), "contactServer");
        final ActorRef client = system.actorOf(ContactClient.props(), "contactClient");

        // populate
        server.tell(new PutMsg("Alice", "alice@example.com"), ActorRef.noSender());
        server.tell(new PutMsg("Bob", "bob@example.com"), ActorRef.noSender());

        // queries: the sender is the client so replies go there
        server.tell(new GetMsg("Alice"), client);
        server.tell(new GetMsg("Eve"), client);
        server.tell(new GetMsg("Bob"), client);

        // wait a bit for messages to be processed (deterministic small delay)
        Thread.sleep(800);

        system.terminate();
        system.getWhenTerminated().toCompletableFuture().get();
    }
}