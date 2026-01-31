package com.css.challenge.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.css.challenge.client.Order;

public class ShelfStorage {

	   TreeMap<Order, String> treemap; // storage for the whole shelf
	   TreeMap<Order, String> HCtreemap; //duplicate storage for non-room temperature order
	   
	   public ShelfStorage() {
		super();
		Comparator<Order> comparator = new DurationComparator();
		this.HCtreemap = new TreeMap<>(comparator);
		this.treemap = new TreeMap<>(comparator);		
	   }  
	   
	   public synchronized void add(Order order) {	
		   order.setStorage("shelf");
		   if(order.getTemp().equals("hot") || order.getTemp().equals("cold") ) { 	  
			   HCtreemap.put(order, order.getId());
		   }
		   treemap.put(order, order.getId());
	   }
       // Returning true if hot or cool orders is successfully moved to heater or cooler
	   public synchronized boolean moveHotOrCold(Map<String, Order> heater, Map<String, Order> cooler) {
			if (!HCtreemap.isEmpty()) {
				
				Order hotOrCold = HCtreemap.firstKey();
				Map<String, Order> coolerOrHeater = (hotOrCold.getTemp().equals("hot")) ? heater : cooler;
				int size = coolerOrHeater.size();
				if (size < 6) {
					Order order = HCtreemap.pollFirstEntry().getKey();
					treemap.remove(order);
					
					// Move to a new storage and reset the order storage name
					coolerOrHeater.put(order.getId(), order);
					order.setStorage(hotOrCold.getTemp());
					return true;
				}
			}
			return false;
		}

	   public synchronized void removeOrder(Order order) {
			
			synchronized (this) {
				if (order.getTemp().equals("hot") || order.getTemp().equals("cold")) {
					HCtreemap.remove(order);
				}
				treemap.remove(order);
			}
		}
	   
		public synchronized Order discard() {
			Order order = null;
			order = treemap.pollFirstEntry().getKey();
		    HCtreemap.remove(order);
			return order;
		}
 
		public synchronized boolean hasHotOrCold() {
			return !HCtreemap.isEmpty();
		}
		
		public  int size() {
			return treemap.size();
		}
				
		public synchronized boolean contains(Order order) {			
			if(treemap.containsKey(order)) {
				return true;
			}
			return false;
		}	  	   
}
