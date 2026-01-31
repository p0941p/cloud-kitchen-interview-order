package com.css.challenge.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.css.challenge.client.Order;

public class ShelfStorage {
	
	    Map<Order, Integer> mapToList;
	    SortedSet<Order> sortedSet;
	    List<Order> hotOrColdList;
	   
	   public ShelfStorage() {
		super();
		Comparator<Order> comparator = new DurationComparator();
		this.mapToList = new ConcurrentHashMap<>();
		//this.sortedSet = new ConcurrentSkipListSet<>(comparator);
		this.sortedSet = Collections.synchronizedSortedSet(new TreeSet<>(comparator));
		this.hotOrColdList = Collections.synchronizedList(new ArrayList<Order>());
	   }  
	   
	   public synchronized void add(Order order) {	
		   order.setStorage("shelf");
		   if(order.getTemp().equals("hot") || order.getTemp().equals("cold") ) { 
			  
			   hotOrColdList.add(order);
			   mapToList.put(order, hotOrColdList.indexOf(order));
		   }
		   sortedSet.add(order);
		 
		   System.out.println();
		   
	   }
	   
		public synchronized boolean moveHotOrCold(Map<String, Order> heater, Map<String, Order> cooler) {
			if (!hotOrColdList.isEmpty()) {

				// int indexTobeRemoved = hotOrColdList.size() -1 ;
				// Order hotOrCold = hotOrColdList.removeLast();
				// mapToList.remove(indexTobeRemoved);
				Order hotOrCold = hotOrColdList.getLast();
				Map<String, Order> coolerOrHeater = (hotOrCold.getTemp().equals("hot")) ? heater : cooler;
				int size = coolerOrHeater.size();
				if (size < 6) {
					int indexTobeRemoved = hotOrColdList.size() - 1;
					Order order = hotOrColdList.removeLast();
					mapToList.remove(indexTobeRemoved);
					sortedSet.remove(order);

					// Move to a new storage and reset the order storage name
					coolerOrHeater.put(hotOrCold.getId(), hotOrCold);
					hotOrCold.setStorage(hotOrCold.getTemp());
					return true;
				}
			}
			return false;
		}
	   
		public synchronized void removeOrder(Order order) {
			if (order.getTemp().equals("hot") || order.getTemp().equals("cold")) {
				int hotOrColdListIndex = mapToList.get(order);
				hotOrColdList.remove(hotOrColdListIndex);
			}
			sortedSet.remove(order);
//			System.out.println(sortedSet);
			System.out.println();
		}
        
		public synchronized Order discard() {
			Order order = null;
			order = sortedSet.first();
			int hotOrColdListIndex = mapToList.get(order);
			hotOrColdList.remove(hotOrColdListIndex);
			sortedSet.removeFirst();
			return order;
		}
 
		public synchronized boolean hasHotOrCold() {
			return !hotOrColdList.isEmpty();
		}
		
		public synchronized int size() {
			return sortedSet.size();
		}
		
		
		public synchronized boolean contains(Order order) {
			
			if(sortedSet.contains(order)) {
				return true;
			}
			return false;
		}
		  	   
}
