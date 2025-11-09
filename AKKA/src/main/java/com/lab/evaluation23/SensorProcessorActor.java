package com.lab.evaluation23;

import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class SensorProcessorActor extends AbstractActor {

	private double currentAverage;
	private List<Integer> temperatures;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMsg.class, this::gotData).build();
	}

	private void gotData(TemperatureMsg msg) throws Exception {
		System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " + msg.getSender());
		
		// get the temperature value
		int temperature = msg.getTemperature();
		
		// error handling for negative temperatures
		if (temperature < 0) {
			throw new Exception("Invalid temperature: " + temperature);
		}

		// update the list of temperatures
		temperatures.add(temperature);	

		// compute the new average
		double sum = 0;
		for (int temp : temperatures) {
			sum += temp;
		}
		currentAverage = sum / temperatures.size();

		System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " + currentAverage);
	}

	static Props props() {
		return Props.create(SensorProcessorActor.class);
	}

	public SensorProcessorActor() {
	}
}
