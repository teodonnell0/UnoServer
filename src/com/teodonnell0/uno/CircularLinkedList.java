package com.teodonnell0.uno;

import java.util.LinkedList;

public class CircularLinkedList<T> extends LinkedList<T> {
	private static final long serialVersionUID = 1L;
	private int counter = 0;
	
	public T getNext() {
		safeIncrementCounter();
		return get(counter);
	}
	
	public T getPrevious() {
		safeDecrementCounter();
		return get(counter);
	}
	
	private void safeDecrementCounter() {
		--counter;
		if(counter < 0) {
			counter = size() - 1;
		}
	}
	
	private void safeIncrementCounter() {
		counter = (++counter % size());
	}
}
