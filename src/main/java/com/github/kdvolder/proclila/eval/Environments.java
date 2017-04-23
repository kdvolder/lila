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

	private abstract class AbstractEnv implements Environment {
		
		@Override
		public final Computation<Object> def(TerminalNode id, Object value) {
			try {
				def(id.getText(), value);
				return computations.just(value);
			} catch (Exception e) {
				return computations.fail(new ProcLiLaException(id, e));
			}
		}
		
		@Override
		public Environment extend() {
			return new ExtendedEnv(this);
		}
	}

	private class ExtendedEnv extends AbstractEnv {

		private final Environment parent;
		private final Map<String, Object> frame = new HashMap<>();
		
		public ExtendedEnv(Environment parent) {
			this.parent = parent;
		}

		@Override
		public Computation<Object> def(String id, Object value) {
			String key = id;
			if (frame.containsKey(key)) {
				return computations.fail(new Exception("'"+id+"' is already defined"));
			}
			frame.put(key, value);
			return computations.just(value);
		}

		@Override
		public Computation<Object> get(TerminalNode id) {
			String key = id.getText();
			if (frame.containsKey(key)) {
				return computations.just(frame.get(key));
			}
			return parent.get(id);
		}
	}

	private class EmtpyEnv extends AbstractEnv {

		@Override
		public Computation<Object> def(String id, Object value) {
			return computations.fail(new Exception("Can't add definition '"+id+"' to a closed Env!"));
		}

		@Override
		public Computation<Object> get(TerminalNode id) {
			return computations.fail(new ProcLiLaException(id, "Unknown variable: "+id));
		}

	}

	public Environment global() {
		try {
			Environment g = new EmtpyEnv()
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
