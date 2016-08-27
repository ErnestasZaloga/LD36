package com.company.minery.utils;

public final class ThreadSafeArray<T> {
	
	private volatile T[] array = (T[])(new Object[8]);
	private volatile int size;
	
	public synchronized void add(final T value) {
		if(size == array.length) {
			final T[] newArray = (T[])(new Object[array.length * 2]);
			System.arraycopy(array, 0, newArray, 0, array.length);
			array = newArray;
		}
		
		array[size] = value;
		size += 1;
	}
	
	public synchronized T get(final int index) {
		if(index >= size) {
			throw new IndexOutOfBoundsException("index out of bounds. index = " + index + ", size = " + size);
		} 
		return array[index];
	}
	
	public synchronized void clear() {
		size = 0;
	}
	
	public synchronized int size() {
		return size;
	}
	
}
