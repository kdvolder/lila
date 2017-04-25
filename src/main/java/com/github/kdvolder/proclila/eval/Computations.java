package com.github.kdvolder.proclila.eval;

import java.util.concurrent.Callable;
import java.util.function.Function;

import reactor.util.function.Tuple2;

public abstract class Computations<EnvVal> {
	
	public interface Computation<T> {
		<R> Computation<R> then(Function<T, Computation<R>> fun);
		<R> Computation<R> then(
				Function<T, Computation<R>> onSuccess,
				Function<Throwable, Computation<R>> onFail
		);
		<R> Computation<Tuple2<T,R>> and(Computation<R> cl);
	}
	
	public abstract Computation<Void> fromRunnable(Runnable callable);
	public abstract <T> Computation<T> fromCallable(Callable<T> callable);
	public abstract <T> Computation<T> just(T value);
	public abstract <T> Computation<T> fail(Throwable procLiLaException);
	public abstract <T> Computation<T> withEnv(Function<Environment<EnvVal>, Computation<T>> fun);
	public abstract <T> Computation<T> useEnv(Computation<T> c, Environment<EnvVal> env);
	
//	public abstract <T> Object run(Computation<T> computation) throws Exception;

}
