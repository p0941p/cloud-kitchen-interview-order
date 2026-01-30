package com.css.challenge.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import com.css.challenge.client.Action;
import com.css.challenge.client.Order;


public class Tools {

    public static void discardNPlace(List<Action> actions, Instant epochTime, PriorityBlockingQueue<Order> heap, Order order) {
    	
    	//Discard order
    	Order toBeDiscard = heap.peek();
    	heap.poll();
    	Action actionDiscard = new Action(epochTime, toBeDiscard.getId(), "discard", "shelf");
    	System.out.println("Action: " + actionDiscard); 
    	actions.add(actionDiscard);
    	
    	//Add order
    	heap.offer(order);    	
    	Action actionPlace = new Action(epochTime, order.getId(), "place", "shelf");
    	order.setStorage("shelf");
    	System.out.println("Action: " + actionPlace); 
    	actions.add(actionPlace);   	
    }
    

    public static long getInterval(Duration max, Duration min) {
    	 return  (long)((Math.random() * (max.toMillis() - min.toMillis())) + min.toMillis());   
    }
    
    public static boolean isFresh(Order order) {
    	
    	Instant expiration = order.getTimestamp().plusSeconds(order.getFreshness());	
    	if(Instant.now().isAfter(expiration)) {
    		return false;
    	}
    	return true;   	
    }
  
    public static void placeOnShelfFromHC(Order order, PriorityBlockingQueue<Order> shelf, List<Action> actions, Instant epochTime,Map<String, Order> cooler, Map<String, Order> heater) {
           if(shelf.size() < 12) {
        	   shelf.add(order);
       		Action action = new Action(epochTime, order.getId(), "place", "shelf");
       		System.out.println("Action: " + action); 
       		// Add target to order storage
       		order.setStorage("shelf");		
       		actions.add(action);
           } else {
        	   Tools.discardNPlace(actions,epochTime, shelf, order);
           }
    }
    
    public static void placeOnShelf(Order order, PriorityBlockingQueue<Order> shelf, List<Action> actions, Instant epochTime,Map<String, Order> cooler, Map<String, Order> heater) {
    	if(shelf.size() < 12) {
    		shelf.add(order);
    		Action action = new Action(epochTime, order.getId(), "place", "shelf");
    		System.out.println("Action: " + action); 
    		// Add target to order storage
    		order.setStorage("shelf");		
    		actions.add(action);
		} else {		
			if(!order.getTemp().equals("room")) {
				Map<String, Order> coolerOrHeater = (order.getTemp().equals("hot"))? heater : cooler; 
			    int size = coolerOrHeater.size();
			    if(size < 6) {
				    placeOnHeaterCoolerOnly(order, coolerOrHeater, actions, epochTime);
			    } else {
			    Tools.discardNPlace(actions,epochTime, shelf, order);
			    }
			} else {
				Tools.discardNPlace(actions,epochTime, shelf, order);
			}
		}
    }
    
    public static void placeOnHeaterCoolerOnly(Order o,Map<String, Order> coolerOrHeater,  List<Action> actions,Instant timestamp) {
    	coolerOrHeater.put(o.getId(), o);
    	String target = (o.getTemp().equals("hot"))? "heater" : "cooler"; 
		Action action = new Action(timestamp, o.getId(), "place", target);
		System.out.println("Action: " + action);    
		actions.add(action);
        // Add target to order storage
		o.setStorage(target);
    }
  }
