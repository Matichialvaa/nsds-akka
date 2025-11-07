package com.clientserver;
import akka.actor.AbstractActor;
import akka.actor.Props;

public class ContactClient extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(GetResponse.class, this::onGetResponse)
            .build();
    }

    void onGetResponse(GetResponse response) {
        System.out.println("Received contact: " + response.name + " -> " + response.email.orElse("No email"));
    }

    public static Props props() {
        return Props.create(ContactClient.class);
    }
}
