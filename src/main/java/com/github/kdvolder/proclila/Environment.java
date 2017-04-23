package com.github.kdvolder.proclila;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.kdvolder.proclila.Computations.Computation;

public interface Environment {

	Computation<Object> def(TerminalNode id, Object value);
	Computation<Object> def(String name, Object value);

	/**
	 * Adds a new empty frame to environment.
	 */
	Environment extend();

	Computation<Object> get(TerminalNode terminalNode);

}
