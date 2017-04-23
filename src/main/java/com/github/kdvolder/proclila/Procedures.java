package com.github.kdvolder.proclila;

import com.github.kdvolder.proclila.Computations.Computation;

public class Procedures {
	
	private Computations computations;

	public Procedures(Computations computations) {
		this.computations = computations;
	}

	public Proc binary(String name, BinaryOperator binOp) {
		return named("name", (a) -> 
			computations.just(unary("("+name+" "+a+")", (b) -> {
				return binOp.apply(a, b);
			}))
		);
	}

	private Proc unary(String name, UnaryOperator op) {
		return named(name, a -> 
			computations.fromCallable(() -> op.apply(a))
		);
	}

	public Proc named(String name, Proc proc) {
		return new Proc() {
			@Override
			public Computation<Object> apply(Object arg) {
				return proc.apply(arg);
			}
			
			@Override
			public String toString() {
				return "#proc:"+name;
			}
		};
	}

}
