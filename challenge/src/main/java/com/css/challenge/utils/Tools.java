package com.css.challenge.utils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

import com.css.challenge.client.Action;
import com.css.challenge.client.Order;


public class Tools {

    public synchronized static void discardNPlace(List<Action> actions, Instant epochTime, PriorityBlockingQueue<Order> heap, Order o) {
    	Order toBeDiscard = heap.peek();
    	Action actionDiscard = new Action(epochTime, toBeDiscard.getId(), "discard", "shelf");
    	actions.add(actionDiscard);
    	heap.poll();
    	Action actionPlace = new Action(epochTime, o.getId(), "place", "shelf");
    	actions.add(actionPlace);
    	heap.offer(o);
    	
    }
    
    public static boolean isFresh(Order o) {
    	
    	//LocalDateTime expiration = o.getTimestamp().plusSeconds(o.getFreshness());
    	//if(LocalDateTime.now().isAfter(expiration)) {
    	
    	Instant expiration = o.getTimestamp().plusSeconds(o.getFreshness());
    	
    	if(Instant.now().isAfter(expiration)) {
    		return false;
    	}
    	return true;   	
    }
  
    public synchronized static void placeOnShelf(Order o, PriorityBlockingQueue<Order> shelf, List<Action> actions, Instant epochTime,Map<String, Order> cooler, Map<String, Order> heater) {
    	if(shelf.size() < 12) {
    		Action action = new Action(epochTime, o.getId(), "place", "shelf");
			shelf.add(o);
    		//storageLookUp.put(o.getId(), "shelf");
			o.setStorage("shelf");
    		
    		actions.add(action);
		} else {		
			if(o.getTemp().equals("room")) {
				Tools.discardNPlace(actions,epochTime, shelf, o);
			} else {
			    Map<String, Order> coolerOrHeater = (o.getTemp().equals("hot"))? heater : cooler; 
			    if(coolerOrHeater.size()<6) {
				    placeOnHeaterCoolerOnly(o, coolerOrHeater, actions, epochTime);
			    } else {
			    Tools.discardNPlace(actions,epochTime, shelf, o);
			    o.setStorage("shelf");
			    }
			}
		}
    }
    public synchronized static void placeOnHeaterCoolerOnly(Order o,Map<String, Order> coolerOrHeater,  List<Action> actions,Instant timestamp) {
    	String target = (o.getTemp().equals("hot"))? "heater" : "cooler"; 
		Action action = new Action(timestamp, o.getId(), "place", target);
    	coolerOrHeater.put(o.getId(), o);
    
		actions.add(action);
		//storageLookUp.put(o.getId(), target);
		o.setStorage(target);
    }
  }
