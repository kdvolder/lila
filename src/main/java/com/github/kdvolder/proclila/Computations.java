package com.github.kdvolder.proclila;

import java.util.concurrent.Callable;
import java.util.function.Function;

import reactor.util.function.Tuple2;

public abstract class Computations {
	
	public interface Computation<T> {
		<R> Computation<R> then(Function<T, Computation<R>> fun);
		<R> Computation<R> then(
				Function<T, Computation<R>> onSuccess,
				Function<Throwable, Computation<R>> onFail
		);
		<R> Computation<Tuple2<T,R>> and(Computation<R> cl);
		Computation<T> useEnv(Environment env);
	}
	
	public abstract Computation<Void> fromRunnable(Runnable callable);
	public abstract <T> Computation<T> fromCallable(Callable<T> callable);
	public abstract <T> Computation<T> just(T value);
	public abstract <T> Computation<T> fail(Throwable procLiLaException);
	public abstract <T> Computation<T> withEnv(Function<Environment, Computation<T>> fun);
	public abstract <T> Object run(Computation<T> computation) throws Exception;

}
