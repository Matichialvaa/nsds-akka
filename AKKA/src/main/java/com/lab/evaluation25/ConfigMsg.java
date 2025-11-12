package com.lab.evaluation25;

import akka.actor.ActorRef;

public class ConfigMsg { 
    private ActorRef balancer;

    public ConfigMsg(ActorRef balancer) {
        this.balancer = balancer;
    }

    public ActorRef getBalancer() {
        return balancer;
    }
}
