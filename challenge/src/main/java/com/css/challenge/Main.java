package com.css.challenge;

import com.css.challenge.client.Action;
import com.css.challenge.client.Client;
import com.css.challenge.client.Order;
import com.css.challenge.client.Problem;
import com.css.challenge.utils.DurationComparator;
import com.css.challenge.utils.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  String auth = "uy4bbtwjtpmy";

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
      Map<String, Order> heater = new HashMap<>();
      Map<String, Order> cooler = new HashMap<>();
      PriorityQueue<Order> shelf = new PriorityQueue<>(12, comparator);
	  //ExecutorService executor = Executors.newFixedThreadPool(20);
      ExecutorService executor = Executors.newCachedThreadPool();
	
      List<Action> actions = new ArrayList<>();
      for (Order order : problem.getOrders()) {
        LOGGER.info("Received: {}", order);

      //    actions.add(new Action(Instant.now(), order.getId(), Action.PLACE, Action.COOLER));
        Thread.sleep(rate.toMillis());
      }
      
	  for (Order order : problem.getOrders()) {
		Instant timestamp = Instant.now();
		order.setTimestamp(timestamp);
				
		if(!order.getTemp().equals("room")) {
		    Map<String, Order> coolerOrHeater = (order.getTemp().equals("hot"))? heater : cooler; 
		    if(coolerOrHeater.size()<6) {
			    Tools.placeOnHeaterCoolerOnly(order, coolerOrHeater, actions, timestamp);
		    } else {
				order.setFreshness(order.getFreshness()/2);
			    Tools.placeOnShelf(order, shelf,  actions, timestamp, cooler, heater);
			} 
		} else {
			Tools.placeOnShelf(order, shelf,  actions, timestamp, cooler, heater);
		}
		Callable<String> pickOrders = () -> pickUpOrder2(order, min, max, actions, cooler, heater, shelf);						
		Thread.sleep(rate);
		Future<String> result = executor.submit(pickOrders);
	  }
	  executor.shutdown();
	  executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	  
      String result = client.solveProblem(problem.getTestId(), rate, min, max, actions);
      LOGGER.info("Result: {}", result);

     } catch (IOException | InterruptedException e) {
          LOGGER.error("Simulation failed: {}", e.getMessage());
     }
  }
	static String pickUpOrder2(Order order, Duration min, Duration max, List<Action> actions,Map<String, Order> cooler, Map<String, Order> heater, PriorityQueue<Order> shelf) {
		
		try {
			Thread.sleep(min.toMillis());
			Instant timestamp = Instant.now();
			pickUpOrder(timestamp, actions,cooler, heater, order, shelf);		
		} catch (InterruptedException e) {
			   Thread.currentThread().interrupt();
			   return "e";
		}		
		return "pickup thread of "+ order + "is done";			
	}
	
	

	private static void pickUpOrder(Instant timestamp, List<Action> actions,Map<String, Order> cooler, Map<String, Order> heater, Order order, PriorityQueue<Order> shelf) {
		
		//long epochTimeMicroSecond = ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
		  
		   Action action;
		/*   
		   DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			  Instant instant = Instant.now();
			  ZonedDateTime localZonedTime = instant.atZone(ZoneId.systemDefault());
			  String formattedTime = localZonedTime.format(formatter);
		 */  
		   if(!Tools.isFresh(order)) {		   
			   if(order.getStorage().equals("heater")) {
				   action = new Action(timestamp, order.getId(), "discard", "heater");
				      heater.remove(order.getId());
				     
			   } else if(order.getStorage().equals("cooler")) {
				   action = new Action(timestamp, order.getId(), "discard", "cooler");
				      cooler.remove(order.getId());
				     
			   } else {
				    action = new Action(timestamp, order.getId(), "discard", "shelf");
				      shelf.remove(order);
				  
			   }
		   } else {
			   if(order.getStorage().equals("heater")) {
				   action = new Action(timestamp, order.getId(), "pickup", "heater");
			      heater.remove(order.getId());
			    
		       } else if(order.getStorage().equals("cooler")) {
		    	   action = new Action(timestamp, order.getId(), "pickup", "cooler");
			      cooler.remove(order.getId());
			     
		      } else {
			      shelf.remove(order);
			      action = new Action(timestamp, order.getId(), "pickup", "shelf");
		      }
		   }
		   actions.add(action);
		 //   System.out.println(action + "timestamp: "+formattedTime);
	}

  public static void main(String[] args) {
    new CommandLine(new Main()).execute(args);
  }
}
