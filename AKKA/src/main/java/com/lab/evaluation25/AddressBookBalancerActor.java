package com.lab.evaluation25;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.pattern.Patterns;

public class AddressBookBalancerActor extends AbstractActor {

	ActorRef worker1 = null;
	ActorRef worker0 = null;	

	public AddressBookBalancerActor() {
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GetMsg.class, this::routeQuery)
				.match(PutMsg.class, this::storeEntry)
				.build();
	}

	int splitByInitial(String s) {
		char firstChar = s.charAt(0);

		// Normalize case for comparison
		char upper = Character.toUpperCase(firstChar);

		if (upper >= 'A' && upper <= 'M') {
			return 0;
		} else {
			return 1;
		}
	}

	void routeQuery(GetMsg msg) {		
		System.out.println("BALANCER: Received query for name " + msg.getName());
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, TimeUnit.SECONDS);
		try {
			// ask the first worker, if it has return, if no ask the second worker.
			scala.concurrent.Future<Object> waitingForWorker = Patterns.ask(worker0, msg, 5000);
			Object result = (Object) waitingForWorker.result(timeout, null);

			if (result == null) {
				waitingForWorker = Patterns.ask(worker1, msg, 5000);
				Object result2 = (Object) waitingForWorker.result(timeout, null);
				getSender().tell(result2, getSelf());
			} 

			// if no copies, just return timeout
			System.out.println("BALANCER: Both copies are resting for name " + msg.getName() + "!");
			getSender().tell(result, getSelf());
		} catch (TimeoutException | InterruptedException e) {
				System.out.println("CLIENT: Received timeout message");
			e.printStackTrace();
		}
	}

	void storeEntry(PutMsg msg) {
		System.out.println("BALANCER: Received new entry " + msg.getName() + " - " + msg.getEmail());
		worker0.tell(msg, getSelf());
		worker1.tell(msg, getSelf());
	}

	static Props props() {
		return Props.create(AddressBookBalancerActor.class);
	}
}
