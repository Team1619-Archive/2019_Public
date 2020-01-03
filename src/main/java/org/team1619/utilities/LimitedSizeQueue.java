package org.team1619.utilities;

import java.util.concurrent.LinkedBlockingQueue;

public class LimitedSizeQueue<E> extends LinkedBlockingQueue<E> {

	private final int fLimit;

	public LimitedSizeQueue(int limit) {
		fLimit = limit;
	}

	@Override
	public boolean add(E element) {
		boolean added = super.add(element);
		while (added && size() > fLimit) {
			super.remove();
		}
		return added;
	}
}
