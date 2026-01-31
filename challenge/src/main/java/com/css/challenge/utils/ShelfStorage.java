package com.css.challenge.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.css.challenge.client.Order;

public class ShelfStorage {
	
	    Map<Order, Integer> mapToList;
	    public SortedSet<Order> getSortedSet() {
			return sortedSet;
		}

		public List<Order> getHotOrColdList() {
			return hotOrColdList;
		}

		

		SortedSet<Order> sortedSet;
		SortedSet<Order> HCsortedSet;
		
	    TreeMap<Order, String> treemap = new TreeMap<>();
	    TreeMap<Order, String> HCtreemap = new TreeMap<>();
	    List<Order> hotOrColdList;
	   
	   public ShelfStorage() {
		super();
		Comparator<Order> comparator = new DurationComparator();
		/*
		this.mapToList = new ConcurrentHashMap<>();
		//this.sortedSet = new ConcurrentSkipListSet<>(comparator);
		this.sortedSet = Collections.synchronizedSortedSet(new TreeSet<>(comparator));
		this.hotOrColdList = Collections.synchronizedList(new ArrayList<Order>());
		*/
		this.mapToList = new HashMap<>();
		this.hotOrColdList = new ArrayList<>();
		this.sortedSet = new TreeSet<>(comparator);
		
		this.HCtreemap = new TreeMap<>(comparator);
		this.treemap = new TreeMap<>(comparator);
		
	   }  
/*	   
	   public synchronized void add(Order order) {	
		   order.setStorage("shelf");
		   if(order.getTemp().equals("hot") || order.getTemp().equals("cold") ) { 
			  
			   hotOrColdList.add(order);
			   mapToList.put(order, hotOrColdList.indexOf(order));
		   }
		   sortedSet.add(order);
		   if(order.getTemp().equals("hot") || order.getTemp().equals("cold") ) { 
		   if(!sortedSet.contains(order) || !hotOrColdList.contains(order)) {
			   System.out.println("************NOt added" + order);
		   }}
	   }
*/	   
	   public synchronized void add(Order order) {	
		   order.setStorage("shelf");
		   if(order.getTemp().equals("hot") || order.getTemp().equals("cold") ) { 	  
			   HCtreemap.put(order, order.getId());
		   }
		   treemap.put(order, order.getId());
		
	   }
	   /*
		public synchronized boolean moveHotOrCold(Map<String, Order> heater, Map<String, Order> cooler) {
			if (!hotOrColdList.isEmpty()) {

				// int indexTobeRemoved = hotOrColdList.size() -1 ;
				// Order hotOrCold = hotOrColdList.removeLast();
				// mapToList.remove(indexTobeRemoved);
				Order hotOrCold = hotOrColdList.getLast();
				Map<String, Order> coolerOrHeater = (hotOrCold.getTemp().equals("hot")) ? heater : cooler;
				int size = coolerOrHeater.size();
				if (size < 6) {
					//int indexTobeRemoved = hotOrColdList.size() - 1;
					Order order = hotOrColdList.removeLast();
					mapToList.remove(order);
					sortedSet.remove(order);

					// Move to a new storage and reset the order storage name
					coolerOrHeater.put(order.getId(), order);
					order.setStorage(hotOrCold.getTemp());
					return true;
				}
			}
			return false;
		}
	   */
	   public synchronized boolean moveHotOrCold(Map<String, Order> heater, Map<String, Order> cooler) {
			if (!HCtreemap.isEmpty()) {
				
				Order hotOrCold = HCtreemap.firstKey();
				Map<String, Order> coolerOrHeater = (hotOrCold.getTemp().equals("hot")) ? heater : cooler;
				int size = coolerOrHeater.size();
				if (size < 6) {
					//int indexTobeRemoved = hotOrColdList.size() - 1;
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
	   /*
		public synchronized void removeOrder(Order order, List<String> pickupFired) {
		
			synchronized (this) {
				if (order.getTemp().equals("hot") || order.getTemp().equals("cold")) {
					pickupFired.add(order.getId());
					int hotOrColdListIndex = mapToList.get(order);
					hotOrColdList.remove(hotOrColdListIndex);
				}
				sortedSet.remove(order);
//			System.out.println(sortedSet);
				if (sortedSet.contains(order) || hotOrColdList.contains(order)) {
					System.out.println("************NOt Removed" + order);
					if (hotOrColdList.contains(order))
						hotOrColdList.remove(mapToList.get(order));
					sortedSet.remove(order);
				}
			}
		}
        */
	   public synchronized void removeOrder(Order order, List<String> pickupFired) {
			
			synchronized (this) {
				if (order.getTemp().equals("hot") || order.getTemp().equals("cold")) {
					pickupFired.add(order.getId());
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
