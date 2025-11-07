package com.clientserver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.OneForOneStrategy;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

public class ContactSupervisor extends AbstractActor {

    private final boolean useResume;
    private ActorRef child;

    public ContactSupervisor(boolean useResume) {
        this.useResume = useResume;
    }

    public static Props propsResume() {
        return Props.create(ContactSupervisor.class, () -> new ContactSupervisor(true));
    }

    public static Props propsRestart() {
        return Props.create(ContactSupervisor.class, () -> new ContactSupervisor(false));
    }

    @Override
    public void preStart() {
        // create a child contact server WITHOUT a fixed name to avoid collisions
        child = getContext().actorOf(ContactServer.props());
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
            10,
            Duration.create("1 minute"),
            DeciderBuilder
                .match(Exception.class, ex -> useResume ? SupervisorStrategy.resume() : SupervisorStrategy.restart())
                .matchAny(o -> SupervisorStrategy.escalate())
                .build()
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchAny(msg -> child.forward(msg, getContext()))
            .build();
    }
}