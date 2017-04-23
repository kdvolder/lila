package com.github.kdvolder.proclila.eval;

@FunctionalInterface
public interface BinaryOperator {
	Object apply(Object a, Object b) throws Exception;
}
