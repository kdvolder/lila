package com.github.kdvolder.proclila;

@FunctionalInterface
public interface BinaryOperator {
	Object apply(Object a, Object b) throws Exception;
}
