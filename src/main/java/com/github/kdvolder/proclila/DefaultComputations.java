package com.github.kdvolder.proclila;

import java.util.concurrent.Callable;
import java.util.function.Function;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class DefaultComputations extends Computations {

	private Procedures procedures = new Procedures(this);
	private Environments envs = new Environments(this, procedures);
	
	@FunctionalInterface
	private interface EnvComputeFunction<T> {
		T compute(Environment env) throws Exception;
	}

	private abstract class EnvMonad<T> implements Computation<T> {
		
		abstract T compute(Environment env) throws Exception;

		@Override
		public <R> Computation<R> then(Function<T, Computation<R>> fun) {
			return envMonad(env -> {
				T self = compute(env);
				EnvMonad<R> result = (EnvMonad<R>) fun.apply(self);
				return result.compute(env);
			});
		}

		@Override
		public <R> Computation<R> then(Function<T, Computation<R>> 
				onSuccess,
				Function<Throwable, Computation<R>> onFail
		) {
			return withEnv(env -> {
				try {
					return onSuccess.apply(compute(env));
				} catch (Throwable e) {
					return onFail.apply(e);
				}
			});
		}

		@Override
		public <R> Computation<Tuple2<T, R>> and(Computation<R> _other) {
			return then(self -> 
				_other.then(other -> just(Tuples.of(self, other)))
			);
		}

		@Override
		public Computation<T> useEnv(Environment env) {
			return fromCallable(() -> compute(env));
		}
	}
		
	private <T> EnvMonad<T> envMonad(EnvComputeFunction<T> computer) {
		return new EnvMonad<T>() {
			@Override T compute(Environment env) throws Exception {
				return computer.compute(env);
			}
		};
	}
		
	@Override
	public Computation<Void> fromRunnable(Runnable runnable) {
		return envMonad(env -> {
			runnable.run();
			return null;
		});
	}

	@Override
	public <T> Computation<T> fromCallable(Callable<T> callable) {
		return envMonad(env -> callable.call());
	}

	@Override
	public <T> Computation<T> just(T value) {
		return envMonad(env -> value);
	}

	@Override
	public <T> Computation<T> fail(Throwable e) {
		return envMonad(env -> {
			if (e instanceof Exception) {
				throw (Exception)e;
			}
			throw new RuntimeException(e);
		});
	}

	@Override
	public <T> Computation<T> withEnv(Function<Environment, Computation<T>> fun) {
		return envMonad(env -> ((EnvMonad<T>)fun.apply(env)).compute(env));
	}

	@Override
	public <T> Object run(Computation<T> computation) throws Exception {
		if (computation instanceof EnvMonad) {
			return ((EnvMonad<T>)computation).compute(envs.global());
		}
		throw new IllegalArgumentException("Don't know how to run that computation: "+computation);
	}


}
