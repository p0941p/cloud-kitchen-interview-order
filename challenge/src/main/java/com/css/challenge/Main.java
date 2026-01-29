package com.css.challenge;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.css.challenge.client.Action;
import com.css.challenge.client.Client;
import com.css.challenge.client.Order;
import com.css.challenge.client.Problem;
import com.css.challenge.utils.DurationComparator;
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
  String endpoint = "https://api.cloudkitchens.com";

  @Option(names = "--auth", description = "Authentication token (required)")
  String auth = "";

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
		try {
			Client client = new Client(endpoint, auth);
			Problem problem = client.newProblem(name, seed);

			// ------ Execution harness logic goes here using rate, min and max ----
			Comparator<Order> comparator = new DurationComparator();
			Map<String, Order> heater = new ConcurrentHashMap<>();
			Map<String, Order> cooler = new ConcurrentHashMap<>();
			PriorityBlockingQueue<Order> shelf = new PriorityBlockingQueue<>(12, comparator);
			ExecutorService executor = Executors.newFixedThreadPool(20);
			// ExecutorService executor = Executors.newCachedThreadPool();
			// ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);
			List<Action> actions = new ArrayList<>();
			for (Order order : problem.getOrders()) {
				LOGGER.info("Received: {}", order);
				// actions.add(new Action(Instant.now(), order.getId(), Action.PLACE,
				// Action.COOLER));
			}

			// for (Order order : problem.getOrders()) {
			// placeOrder(order, heater, cooler, shelf, executor, actions);
			// }
			// executor.shutdown();
			// executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			List<CompletableFuture<?>> pickFutures = new ArrayList<>();
			for (Order o : problem.getOrders()) {
				//long delay = rate.getSeconds();
				/*
				Runnable placeOrders = () -> placeOrder(o, heater, cooler, shelf, actions);
				Runnable pickOrders = () -> pickUpOrder(actions, cooler, heater, o, shelf);
				// executor.schedule(placeOrders, delay, TimeUnit.SECONDS);
				CompletableFuture<?> placeFuture = CompletableFuture.runAsync(placeOrders,executor).thenRunAsync(pickOrders,executor);
				placeFutures.add(placeFuture);
				*/
				//CompletableFuture<?> placeFuture = CompletableFuture.runAsync(placeOrders,executor);
				placeOrder(o, heater, cooler, shelf, actions, executor, pickFutures);
				try {
					Thread.sleep(rate.toMillis());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					LOGGER.error(e.getMessage());
				}
			}
			
            CompletableFuture.allOf(pickFutures.toArray(new CompletableFuture[0])).join();
			for(Action a: actions) {
				System.out.println(a);
			}
			String result = client.solveProblem(problem.getTestId(), rate, min, max, actions);
			LOGGER.info("Result: {}", result);

		} catch (IOException e) {
			LOGGER.error("Simulation failed: {}", e.getMessage());
		}
	}
   
	private synchronized void placeOrder(Order order, Map<String, Order> heater, Map<String, Order> cooler,
			PriorityBlockingQueue<Order> shelf, List<Action> actions, ExecutorService executor, List<CompletableFuture<?>> pickFutures) {
		Instant timestamp = Instant.now();
		order.setTimestamp(timestamp);

		if (!order.getTemp().equals("room")) {
			Map<String, Order> coolerOrHeater = (order.getTemp().equals("hot")) ? heater : cooler;
			if (coolerOrHeater.size() < 6) {
				Tools.placeOnHeaterCoolerOnly(order, coolerOrHeater, actions, timestamp);
			} else {
				order.setFreshness(order.getFreshness() / 2);
				Tools.placeOnShelf(order, shelf, actions, timestamp, cooler, heater);
			}
		} else {
			Tools.placeOnShelf(order, shelf, actions, timestamp, cooler, heater);
		}
		
		try {
			Thread.sleep(min.toMillis());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(e.getMessage());
		}
		Runnable pickOrders = () -> pickUpOrder(actions, cooler, heater,order, shelf);
		CompletableFuture<?> pickFuture = CompletableFuture.runAsync(pickOrders,executor);
		pickFutures.add(pickFuture);
	//	Future<String> result = executor.submit(pickOrders);
		
	}
  /*
	private String pickUpOrderEntry(Order order, Duration min, Duration max, List<Action> actions,
		
		Map<String, Order> cooler, Map<String, Order> heater, PriorityQueue<Order> shelf) {
		try {
			Thread.sleep(min.toMillis());
			Instant timestamp = Instant.now();
			pickUpOrder(timestamp, actions, cooler, heater, order, shelf);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(e.getMessage());
		}
		return "pickup thread of " + order + "is done";
	}
	*/
	

	private synchronized void pickUpOrder( List<Action> actions, Map<String, Order> cooler,
			Map<String, Order> heater, Order order, PriorityBlockingQueue<Order> shelf) {
		Instant timestamp = Instant.now();
		Action action;
		if (!Tools.isFresh(order)) {
			if (order.getStorage().equals("heater")) {
				heater.remove(order.getId());
				action = new Action(timestamp, order.getId(), "discard", "heater");
			} else if (order.getStorage().equals("cooler")) {		
				cooler.remove(order.getId());
				action = new Action(timestamp, order.getId(), "discard", "cooler");
			} else {		
				shelf.remove(order);
				action = new Action(timestamp, order.getId(), "discard", "shelf");
			}
		} else {
			if (order.getStorage().equals("heater")) {			
				heater.remove(order.getId());
				action = new Action(timestamp, order.getId(), "pickup", "heater");
			} else if (order.getStorage().equals("cooler")) {
				cooler.remove(order.getId());
				action = new Action(timestamp, order.getId(), "pickup", "cooler");
			} else {
				shelf.remove(order);
				action = new Action(timestamp, order.getId(), "pickup", "shelf");
			}
		}
		actions.add(action);
	}

	public static void main(String[] args) {
		new CommandLine(new Main()).execute(args);
	}
}
