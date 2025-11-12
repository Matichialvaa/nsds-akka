package com.lab.evaluation25;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeoutException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;

public class AddressBookClientActor extends AbstractActor {

	private scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(3, SECONDS);
	public ActorRef balancer;

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(ConfigMsg.class, this::onConfig)
			.match(PutMsg.class, this::putEntry)
			.match(GetMsg.class, this::query)
			.build();
	}

	private void onConfig(ConfigMsg msg) {
		System.out.println("Configuring balancer!");
		this.balancer = msg.getBalancer();
	}

	void putEntry(PutMsg msg) {
		System.out.println("CLIENT: Sending new entry " + msg.getName() + " - " + msg.getEmail());
		this.balancer.tell(msg, self());
	}

	void query(GetMsg msg) {
		System.out.println("CLIENT: Issuing query for " + msg.getName());
		scala.concurrent.Future<Object> waitingForBalancerFuture = Patterns.ask(balancer, msg, 5000);
		try {
			Object result = (Object) waitingForBalancerFuture.result(timeout, null);
			if (result instanceof ReplyMsg) {
				if (((ReplyMsg) result).getEmail() == null) {
					System.out.println("CLIENT: Received reply, no email found!");
				} else {
					System.out.println("CLIENT: Received reply!");
					System.out.println("CLIENT: Email found: " + ((ReplyMsg) result).getEmail());
				} 
			} else if (result instanceof TimeoutMsg) {
				System.out.println("CLIENT: Received timeout message");
			}
		} catch (TimeoutException | InterruptedException e) {
				System.out.println("CLIENT: Received timeout message");
			e.printStackTrace();
		}
	}

	static Props props() {
		return Props.create(AddressBookClientActor.class);
	}

}

