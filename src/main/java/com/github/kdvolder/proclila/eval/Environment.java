package com.github.kdvolder.proclila.eval;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.kdvolder.proclila.eval.Computations.Computation;

public interface Environment<T> {

	Computation<T> def(TerminalNode id, T value);
	Computation<T> def(String name, T value);

	/**
	 * Adds a new empty frame to environment.
	 */
	Environment<T> extend();

	Computation<T> get(TerminalNode terminalNode);

}
