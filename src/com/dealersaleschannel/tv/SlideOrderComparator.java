package com.dealersaleschannel.tv;

import java.util.Comparator;

public class SlideOrderComparator implements Comparator<Slide> 
{
	@Override
    public int compare(Slide s1, Slide s2) {
		
		Integer s1Order = Integer.parseInt(s1.order);
		Integer s2Order = Integer.parseInt(s2.order);
		
        return s1Order.compareTo(s2Order);
    }
}
