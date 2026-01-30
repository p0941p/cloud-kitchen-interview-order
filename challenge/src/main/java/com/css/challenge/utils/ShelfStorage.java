package com.css.challenge.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.css.challenge.client.Order;

public class ShelfStorage {
	
	  
	   
	  // Map<String, Order> heaterOrCooler = Collections.synchronizedMap(new TreeMap<String, Order>());
	   
	    Map<String, Order> hotOrCold;
	    SortedSet<Order> sortedSet;
	   
	   public ShelfStorage() {
		super();
		Comparator<Order> comparator = new DurationComparator();
		this.hotOrCold = new ConcurrentHashMap<>();
		this.sortedSet = new TreeSet<>(comparator);
	   }  
	   
	   public void add(Order order) {
		   
		   if(order.getTemp().equals("hot") || order.getTemp().equals("cold") ) {
			   hotOrCold.put(order.getId(), order);
		   }
		   sortedSet.add(order);
	   }
	   
		public Order removeHotOrCold() {

			Order order = null;
			if (!hotOrCold.isEmpty()) {
				order = hotOrCold.get(order);
				sortedSet.remove(order);
			}
			return order;
		}

		public void discard() {
			Order order = sortedSet.first();
			hotOrCold.remove(order.getId());
			sortedSet.remove(order);
		}
 
		public boolean hasHotOrCold() {
			return !hotOrCold.isEmpty();
		}
		  	   
}
