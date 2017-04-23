package com.github.kdvolder.proclila.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import reactor.util.function.Tuple2;

public class Functions {

	public static <A,B,R> Function<Tuple2<A,B>,R> fn2(BiFunction<A, B, R> f) {
		return (a) -> f.apply(a.getT1(), a.getT2());
	}

}
