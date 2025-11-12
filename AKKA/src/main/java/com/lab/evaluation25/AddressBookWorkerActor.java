package com.lab.evaluation25;

import java.util.HashMap;

import com.clientserver2.WakeupMsg;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class AddressBookWorkerActor extends AbstractActor {

	private HashMap<String, String> primaryAddresses;
	private HashMap<String, String> replicaAddresses;

	public AddressBookWorkerActor() {
		this.primaryAddresses = new HashMap<String, String>();
		this.replicaAddresses = new HashMap<String, String>();
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

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GetMsg.class, this::generateReply)
				.match(PutMsg.class,  this::saveToAddressBook)
				.match(RestMsg.class, this::onRestMsg)
				.build();
	}

	void onRestMsg(RestMsg msg) {
        System.out.println("Server going to sleep");
        getContext().become(sleeping());
    }

    private Receive sleeping() {
		return receiveBuilder()
				.match(WakeupMsg.class, this::onWakeupMsg)
				.build();
	}

	void onWakeupMsg(WakeupMsg msg) {
        System.out.println("Server waking up");
        getContext().unbecome();
    }
	
	void generateReply(GetMsg msg) {
		System.out.println(this.toString() + ": Received query for name " + msg.getName());
		String email = "";
		if (splitByInitial(msg.getName()) == 0) {
		 	email = primaryAddresses.get(msg.getName());
		} else {
			email = replicaAddresses.get(msg.getName());
		}

		if (email == null) {
			System.out.print("No entry with that name");
		} else {
			getSender().tell(new ReplyMsg(email), getSelf());
		}

	}

	void saveToAddressBook(PutMsg msg) {
		if (splitByInitial(msg.getName()) == 0) {
			primaryAddresses.put(msg.getName(), msg.getEmail());
		} else {
			replicaAddresses.put(msg.getName(), msg.getEmail());
		}
	}

	static Props props() {
		return Props.create(AddressBookWorkerActor.class);
	}
}
