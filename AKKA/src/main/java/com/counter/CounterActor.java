package com.counter;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class CounterActor extends AbstractActorWithStash {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(Increment.class, this::onIncrement)
			.match(Decrement.class, this::onDecrement)
			.match(SimpleMessage.class, this::onMessage)
			.build();
	}

	void onMessage(SimpleMessage msg) {
		onIncrement(new Increment());
	}

	void onIncrement(Increment msg) {
		++counter;
		System.out.println("Counter increased to " + counter);
		unstashAll(); // Unstash all messages when incremented
	}

	void onDecrement(Decrement msg) {
		if (counter == 0) {
			System.out.println("Counter is zero, stashing decrement");
			stash(); // Stash the decrement message if counter is zero
		} else {
			--counter;
			System.out.println("Counter decreased to " + counter);
		}
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
