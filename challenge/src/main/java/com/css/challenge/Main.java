package com.css.challenge;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.css.challenge.client.Action;
import com.css.challenge.client.Client;
import com.css.challenge.client.Order;
import com.css.challenge.client.Problem;
import com.css.challenge.utils.DurationComparator;
import com.css.challenge.utils.ShelfStorage;
import com.css.challenge.utils.Tools;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "challenge", showDefaultValues = true)
public class Main implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  static {
    org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
    System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT: %5$s %n");
  }

  @Option(names = "--endpoint", description = "Problem server endpoint")
  static String endpoint = "https://api.cloudkitchens.com";

  @Option(names = "--auth", description = "Authentication token (required)")
  static String auth = "";

  @Option(names = "--name", description = "Problem name. Leave blank (optional)")
  String name = "";

  @Option(names = "--seed", description = "Problem seed (random if zero)")
  long seed = 0;

  @Option(names = "--rate", description = "Inverse order rate")
  Duration rate = Duration.ofMillis(500);

  @Option(names = "--min", description = "Minimum pickup time")
  Duration min = Duration.ofSeconds(4);

  @Option(names = "--max", description = "Maximum pickup time")
  Duration max = Duration.ofSeconds(8);

  @Override
	public void run() {
	  
	    ExecutorService executor = Executors.newFixedThreadPool(20);
		try {
			Client client = new Client(endpoint, auth);
			Problem problem = client.newProblem(name, seed);

			// ------ Execution harness logic goes here using rate, min and max ----
			Comparator<Order> comparator = new DurationComparator();
			Map<String, Order> heater = new ConcurrentHashMap<>();
			Map<String, Order> cooler = new ConcurrentHashMap<>();
			//PriorityBlockingQueue<Order> shelf = new PriorityBlockingQueue<>(12, comparator);
			ShelfStorage shelf = new ShelfStorage();
			
			List<Action> actions = new ArrayList<>();
			for (Order order : problem.getOrders()) {
				LOGGER.info("Received: {}", order);
			}
			
			for (Order order : problem.getOrders()) {
				// Place Order
				placeOrder(order, heater, cooler, shelf, executor, actions);		
				
				try {
					Thread.sleep(rate);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					LOGGER.error(e.getMessage());
				}
			}
			executor.shutdown();
			executor.awaitTermination(200, TimeUnit.SECONDS);
		
			String result = client.solveProblem(problem.getTestId(), rate, min, max, actions);
			LOGGER.info("Result: {}", result);
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Simulation failed: {}", e.getMessage());
		} finally {
			executor.shutdownNow();
		}
	}
   /*
	private void placeOrder(Order order, Map<String, Order> heater, Map<String, Order> cooler,
			PriorityBlockingQueue<Order> shelf, ExecutorService executor, List<Action> actions) {
		
		Instant timestamp = Instant.now();
		order.setTimestamp(timestamp);
	  
		if (!order.getTemp().equals("room")) {
			Map<String, Order> coolerOrHeater = (order.getTemp().equals("hot")) ? heater : cooler;
			int size = coolerOrHeater.size();
			if (size < 6) {
				Tools.placeOnHeaterCoolerOnly(order, coolerOrHeater, actions, timestamp);
			} else {
				// Heater or Cooler is full, place on shelf and deduct freshness time
				order.setFreshness(order.getFreshness() / 2);
				Tools.placeOnShelfFromHC(order, shelf, actions, timestamp, cooler, heater);
			}
		} else {
			Tools.placeOnShelf(order, shelf, actions, timestamp, cooler, heater);
		}
		Callable<String> pickOrders = () -> pickUpOrderEntry(order, min, max, actions, cooler, heater, shelf);
		Future<String> result = executor.submit(pickOrders);
	}
  */
  private void placeOrder(Order order, Map<String, Order> heater, Map<String, Order> cooler,
			ShelfStorage shelf, ExecutorService executor, List<Action> actions) {
		
		Instant timestamp = Instant.now();
		order.setTimestamp(timestamp);
	  
		if (!order.getTemp().equals("room")) {
			Map<String, Order> coolerOrHeater = (order.getTemp().equals("hot")) ? heater : cooler;
			int size = coolerOrHeater.size();
			if (size < 6) {
				Tools.placeOnHeaterCoolerOnly(order, coolerOrHeater, actions, timestamp);
			} else {
				// Heater or Cooler is full, place on shelf and deduct freshness time
				order.setFreshness(order.getFreshness() / 2);
				Tools.placeOnShelfFromHC(order, shelf, actions, timestamp, cooler, heater);
			}
		} else {
			Tools.placeOnShelf(order, shelf, actions, timestamp, cooler, heater);
		}
		Callable<String> pickOrders = () -> pickUpOrderEntry(order, min, max, actions, cooler, heater, shelf);
		Future<String> result = executor.submit(pickOrders);
	}
  
  
	private String pickUpOrderEntry(Order order, Duration min, Duration max, List<Action> actions,
		
		Map<String, Order> cooler, Map<String, Order> heater, ShelfStorage shelf) {
		try {
			//Wait for Pickup Interval
			long interval = Tools.getInterval(max, min);
			Thread.sleep(interval);
			
			Instant timestamp = Instant.now();
			pickUpOrder(timestamp, actions, cooler, heater, order, shelf);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(e.getMessage());
		}
		return "pickup thread of " + order + "is done";
	}
	
	

	private void pickUpOrder(Instant timestamp, List<Action> actions, Map<String, Order> cooler,
			Map<String, Order> heater, Order order, ShelfStorage shelf) {

		Action action;
		if (!Tools.isFresh(order)) {
			if (order.getStorage().equals("heater")) {
				heater.remove(order.getId());
				action = new Action(timestamp, order.getId(), "discard", "heater");
				System.out.println("Action: " + action);
;			} else if (order.getStorage().equals("cooler")) {		
				cooler.remove(order.getId());
				action = new Action(timestamp, order.getId(), "discard", "cooler");
				System.out.println("Action: " + action);
			} else {		
				shelf.removeOrder(order);
				action = new Action(timestamp, order.getId(), "discard", "shelf");
				System.out.println("Action: " + action);
			}
		} else {
			if (order.getStorage().equals("heater")) {			
				heater.remove(order.getId());
				action = new Action(timestamp, order.getId(), "pickup", "heater");
				System.out.println("Action: " + action);
			} else if (order.getStorage().equals("cooler")) {
				cooler.remove(order.getId());
				action = new Action(timestamp, order.getId(), "pickup", "cooler");
				System.out.println("Action: " + action);
			} else {
				shelf.removeOrder(order);
				action = new Action(timestamp, order.getId(), "pickup", "shelf");
				System.out.println("Action: " + action);
			}
		}
		actions.add(action);
	}

	public static void main(String[] args) {
		new CommandLine(new Main()).execute(args);
	}
}
