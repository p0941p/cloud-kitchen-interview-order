package com.css.challenge;

import com.css.challenge.client.Action;
import com.css.challenge.client.Client;
import com.css.challenge.client.Order;
import com.css.challenge.client.Problem;
import com.css.challenge.utils.DurationComparator;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
      PriorityQueue<Order> shelf = new PriorityQueue<>(10, comparator);
	  ExecutorService executor = Executors.newFixedThreadPool(10);
	
      List<Action> actions = new ArrayList<>();
      for (Order order : problem.getOrders()) {
        LOGGER.info("Received: {}", order);

        actions.add(new Action(Instant.now(), order.getId(), Action.PLACE, Action.COOLER));
        Thread.sleep(rate.toMillis());
      }

      // ----------------------------------------------------------------------
      try {
			Instant timestamp;
		   
			  for (Order order : problem.getOrders()) {
				timestamp = Instant.now();
				o.setTimestamp(timestamp);
				if(o.getId().equals("dxoyb")) {
					System.out.println("");
				}
				//Map<String, Order> coolerOrHeater = (o.getTemp().equals("hot"))? heater : cooler; 
				long epochTimeMicroSecond = LocalDateTime.now().getNano()/1000L;
				if(!o.getTemp().equals("room")) {
				   Map<String, Order> coolerOrHeater = (o.getTemp().equals("hot"))? heater : cooler; 
				   if(coolerOrHeater.size()<6) {
					    Tools.placeOnHeaterCoolerOnly(o, coolerOrHeater, actions, epochTimeMicroSecond);
				   } else {
					    o.setFreshness(o.getFreshness()/2);
					    Tools.placeOnShelf(o, shelf,  actions, epochTimeMicroSecond, cooler, heater);
				   } 
				} else {
					  Tools.placeOnShelf(o, shelf,  actions, epochTimeMicroSecond, cooler, heater);
				}
				Callable<String> pickOrders = () -> pickUpOrder2(o, intervalMin, intervalMax, actions, cooler, heater, shelf);
				Future<String> result = executor.submit(pickOrders);				
				//System.out.println(result.get());				
				try {
					Thread.sleep(rate);
				} catch (InterruptedException e) {
					   Thread.currentThread().interrupt();
					   e.printStackTrace();
				}
			}
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			
			
		/*	for(Action a : actions) {
				   System.out.println(a);
			} */

  } catch (URISyntaxException e) {
	 e.printStackTrace();;
  } catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
  } catch (IOException e) {
	// TODO Auto-generated catch blockcatch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	e.printStackTrace();
  }
 

      String result = client.solveProblem(problem.getTestId(), rate, min, max, actions);
      LOGGER.info("Result: {}", result);

    } catch (IOException | InterruptedException e) {
      LOGGER.error("Simulation failed: {}", e.getMessage());
    }
  }

  public static void main(String[] args) {
    new CommandLine(new Main()).execute(args);
  }
}
