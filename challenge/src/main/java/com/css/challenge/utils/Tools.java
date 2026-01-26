package com.css.challenge.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.css.challenge.client.Action;
import com.css.challenge.client.Order;


public class Tools {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
/*		long testTime = LocalDateTime.now().getNano();
		

Instant instant = Instant.now();
int nanos = instant.getNano();
System.out.println("Nanos within the second: " + nanos);
System.out.println("epoch: " + testTime);
*/
	}
    public static void discardNPlace(List<Action> actions, Instant epochTime, PriorityQueue<Order> heap, Order o) {
    	Order toBeDiscard = heap.peek();
    	heap.poll();
    	Action actionDiscard = new Action(epochTime, toBeDiscard.getId(), "discard", "shelf");
    	actions.add(actionDiscard);
    	heap.offer(o);
    	Action actionPlace = new Action(epochTime, o.getId(), "place", "shelf");
    	actions.add(actionPlace);
    }
    
    public static boolean isFresh(Order o) {
    	
    	LocalDateTime expiration = o.getTimestamp().plusSeconds(o.getFreshness());
    	if(LocalDateTime.now().isAfter(expiration)) {
    		return false;
    	}
    	return true;   	
    }
    
    public static long getInterval(Duration max, Duration min) {
    	 return  (long)((Math.random() * (max.toMillis() - min.toMillis())) + min.toMillis());   
    }
    
    public static void placeOnShelf(Order o, PriorityQueue<Order> shelf, List<Action> actions, Instant epochTime,Map<String, Order> cooler, Map<String, Order> heater) {
    	if(shelf.size() < 12) {
			shelf.add(o);
    		//storageLookUp.put(o.getId(), "shelf");
			o.setStorage("shelf");
    		Action action = new Action(epochTime, o.getId(), "place", "shelf");
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
    public static void placeOnHeaterCoolerOnly(Order o,Map<String, Order> coolerOrHeater,  List<Action> actions,Instant timestamp) {
    	coolerOrHeater.put(o.getId(), o);
    	String target = (o.getTemp().equals("hot"))? "heater" : "cooler"; 
		Action action = new Action(timestamp, o.getId(), "place", target);
		actions.add(action);
		//storageLookUp.put(o.getId(), target);
		o.setStorage(target);
    }
  }
