package com.github.kdvolder.proclila.eval;

import java.util.concurrent.Callable;
import java.util.function.Function;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class DefaultComputations<E> extends Computations<E> {

	@FunctionalInterface
	private interface EnvComputeFunction<E, T> {
		T compute(Environment<E> env) throws Exception;
	}

	public abstract class EnvMonad<T> implements Computation<T> {
		
		abstract T compute(Environment<E> env) throws Exception;

		@Override
		public <R> EnvMonad<R> then(Function<T, Computation<R>> fun) {
			return envMonad((env) -> {
				T self = compute(env);
				EnvMonad<R> result = (EnvMonad<R>) fun.apply(self);
				return result.compute(env);
			});
		}

		@Override
		public <R> EnvMonad<R> then(
				Function<T, Computation<R>>  onSuccess,
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
	}

	@Override
	public <T> EnvMonad<T> useEnv(Computation<T> c, Environment<E> env) {
		return fromCallable(() -> ((EnvMonad<T>)c).compute(env));
	}

	private <T> EnvMonad<T> envMonad(EnvComputeFunction<E, T> computer) {
		return new EnvMonad<T>() {
			@Override T compute(Environment<E> env) throws Exception {
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
	public <T> EnvMonad<T> fromCallable(Callable<T> callable) {
		return envMonad(env -> callable.call());
	}

	@Override
	public <T> EnvMonad<T> just(T value) {
		return envMonad(env -> value);
	}

	@Override
	public <T> EnvMonad<T> fail(Throwable e) {
		return envMonad(env -> {
			if (e instanceof Exception) {
				throw (Exception)e;
			}
			throw new RuntimeException(e);
		});
	}

	@Override
	public <T> EnvMonad<T> withEnv(Function<Environment<E>, Computation<T>> fun) {
		return envMonad(env -> ((EnvMonad<T>)fun.apply(env)).compute(env));
	}

//	@Override
//	public Object run(Computation<Object> computation) throws Exception {
//		if (computation instanceof EnvMonad) {
//			return ((EnvMonad<Object>)computation).compute(envs.global());
//		}
//		throw new IllegalArgumentException("Don't know how to run that computation: "+computation);
//	}
//

}
