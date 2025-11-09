package com.lab.evaluation23;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class DispatcherActor extends AbstractActorWithStash {

	private final static int NO_PROCESSORS = 2;

	// load balancer state
	private Map<ActorRef, List<ActorRef>> processorLoad = new LinkedHashMap<>();

	// Round robin
	private int i = 0;
	private List<ActorRef> processors = new ArrayList<>();

	private static SupervisorStrategy strategy = new OneForOneStrategy(1, Duration.ofMinutes(1),
			DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume()).build());

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	public DispatcherActor() {
		// Create sensor processor actors
		for (int i = 0; i < NO_PROCESSORS; i++) {
			ActorRef processor = getContext().actorOf(SensorProcessorActor.props(), "sensor-processor-" + i);
			processorLoad.put(processor, new ArrayList<>());
			processors.add(processor);
		}
	}

	// Separate Receive behaviors for each dispatch strategy
	private Receive loadBalancerBehavior() {
		return receiveBuilder()
				.match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
				.match(DispatchLogicMsg.class, this::setDispatchLogic)
				.build();
	}

	private Receive roundRobinBehavior() {
		return receiveBuilder()
				.match(TemperatureMsg.class, this::dispatchDataRoundRobin)
				.match(DispatchLogicMsg.class, this::setDispatchLogic)
				.build();
	}

	@Override
	public AbstractActor.Receive createReceive() {
		return receiveBuilder()
				.match(DispatchLogicMsg.class, this::setDispatchLogic)
				.build();
	}

	private void setDispatchLogic(DispatchLogicMsg msg) {
		Receive newBehavior;
		switch (msg.getLogic()) {
			case DispatchLogicMsg.ROUND_ROBIN:
				System.out.println("DISPATCHER: Switching to ROUND ROBIN dispatching");
				newBehavior = roundRobinBehavior();
				break;
			case DispatchLogicMsg.LOAD_BALANCER:
				System.out.println("DISPATCHER: Switching to LOAD BALANCER dispatching");
				newBehavior = loadBalancerBehavior();
				break;
			default:
				throw new IllegalArgumentException("Unknown dispatch logic: " + msg.getLogic());
		}
		getContext().become(newBehavior);
	}

	private void dispatchDataLoadBalancer(TemperatureMsg msg) {
		ActorRef temperatureSensor = msg.getSender();
		if (temperatureSensorAssigned(temperatureSensor)) {
			ActorRef assignedProcessor = getAssignedProcessor(temperatureSensor);
			assignedProcessor.tell(msg, self());
		} else {
			ActorRef leastLoadedProcessor = getLeastLoadedProcessor();
			processorLoad.get(leastLoadedProcessor).add(temperatureSensor);
			leastLoadedProcessor.tell(msg, self());

		}
		
	}

	private ActorRef getLeastLoadedProcessor() {
		return processorLoad.entrySet().stream()
				.min((entry1, entry2) -> Integer.compare(entry1.getValue().size(), entry2.getValue().size()))
				.map(Map.Entry::getKey)
				.orElse(null);
	}

	private ActorRef getAssignedProcessor(ActorRef temperatureSensor) {
		for (Map.Entry<ActorRef, List<ActorRef>> entry : processorLoad.entrySet()) {
			if (entry.getValue().contains(temperatureSensor)) {
				return entry.getKey();
			}
		}
		return null;
	}

	private boolean temperatureSensorAssigned(ActorRef temperatureSensor) {
		for (List<ActorRef> sensors : processorLoad.values()) {
			if (sensors.contains(temperatureSensor)) {
				return true;
			}
		}
		return false;
	}

	private void dispatchDataRoundRobin(TemperatureMsg msg) {
		ActorRef processor = processors.get(i);
		processor.tell(msg, self());
		i = (i + 1) % processors.size();
	}

	static Props props() {
		return Props.create(DispatcherActor.class);
	}
}
