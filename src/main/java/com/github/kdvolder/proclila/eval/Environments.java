package com.github.kdvolder.proclila.eval;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.kdvolder.proclila.eval.Computations.Computation;
import com.github.kdvolder.proclila.util.ProcLiLaException;

public class Environments {

	private final Computations computations;
	private final Procedures procedures;
	
	public Environments(Computations computations, Procedures procedures) {
		this.computations = computations;
		this.procedures = procedures;
	}

	private abstract class AbstractEnv<T> implements Environment<T> {
		
		@Override
		public final Computation<T> def(TerminalNode id, T value) {
			try {
				def(id.getText(), value);
				return computations.just(value);
			} catch (Exception e) {
				return computations.fail(new ProcLiLaException(id, e));
			}
		}
		
		@Override
		public Environment<T> extend() {
			return new ExtendedEnv<>(this);
		}
	}

	private class ExtendedEnv<T> extends AbstractEnv<T> {

		private final Environment<T> parent;
		private final Map<String, T> frame = new HashMap<>();
		
		public ExtendedEnv(Environment<T> parent) {
			this.parent = parent;
		}

		@Override
		public Computation<T> def(String id, T value) {
			String key = id;
			if (frame.containsKey(key)) {
				return computations.fail(new Exception("'"+id+"' is already defined"));
			}
			frame.put(key, value);
			return computations.just(value);
		}

		@Override
		public Computation<T> get(TerminalNode id) {
			String key = id.getText();
			if (frame.containsKey(key)) {
				return computations.just(frame.get(key));
			}
			return parent.get(id);
		}
	}

	private class EmtpyEnv<T> extends AbstractEnv<T> {

		@Override
		public Computation<T> def(String id, Object value) {
			return computations.fail(new Exception("Can't add definition '"+id+"' to a closed Env!"));
		}

		@Override
		public Computation<T> get(TerminalNode id) {
			return computations.fail(new ProcLiLaException(id, "Unknown variable: "+id));
		}

	}

	public Environment<Object> global() {
		try {
			Environment<Object> g = new EmtpyEnv<>()
					.extend();
			binary(g, "==", Objects::equals);
			binary(g, "*", (a, b) -> asNumber(a).multiply(asNumber(b)));
			binary(g, "-", (a, b) -> asNumber(a).subtract(asNumber(b)));
			binary(g, "+", (a, b) -> asNumber(a).add(asNumber(b)));
			return g;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static BigInteger asNumber(Object b) {
		if (b instanceof BigInteger) {
			return (BigInteger) b;
		} else if (b instanceof Integer) {
			return BigInteger.valueOf((Integer) b);
		} else if (b instanceof Long) {
			return BigInteger.valueOf((Long) b);
		}
		throw new IllegalArgumentException("Can't be converted to a number: "+b);
	}

	/**
	 * Create binary operator primitive and add it to given env
	 */ 
	private void binary(Environment env, String name, BinaryOperator binOp) throws Exception {
		env.def(name, procedures.binary(name, binOp));
	}
}
