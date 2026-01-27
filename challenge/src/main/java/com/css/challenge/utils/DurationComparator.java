package com.css.challenge.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import com.css.challenge.client.Order;


public class DurationComparator implements Comparator<Order>{

	@Override
	public int compare(Order x, Order y) {
		
		if(x != null && y!= null) {
			Instant durationX =  x.getTimestamp().plusSeconds(x.getFreshness());
			Instant durationY =  y.getTimestamp().plusSeconds(y.getFreshness());
			if(durationX.isAfter(durationY)) {
				return 1;
			} else if(durationX.isBefore(durationY)){
				return -1;
			}
		    return 0;
		}
        return 0;
	}

}
