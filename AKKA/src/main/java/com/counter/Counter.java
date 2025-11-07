package com.counter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Counter {

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

		// Send messages from multiple threads in parallel
		final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		// test sequence: decrement, decrement, increment, decrement, increment, increment
        List<Object> seq = Arrays.asList(
            new Decrement(),
            new Decrement(),
            new Increment(),
            new Decrement(),
            new Increment(),
            new Increment()
        );

		for (Object msg : seq) {
            exec.submit(() -> counter.tell(msg, ActorRef.noSender()));
        }
		
		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		exec.shutdown();
		sys.terminate();

	}

}
