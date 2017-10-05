package com.github.lhervier.domino.oauth.client.utils;

public class ValueHolder<T> {

	private T value;
	
	public ValueHolder() {}
	
	public ValueHolder(T value) {
		this.value = value;
	}
	
	public T get() {
		return this.value;
	}
	
	public void set(T value) {
		this.value = value;
	}
}
