package com.github.kdvolder.proclila;

import org.antlr.v4.runtime.tree.ParseTree;

public class ProcLiLaException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private ParseTree node;

	public ProcLiLaException(ParseTree node, String msg) {
		super(msg);
		this.node = node;
	}

	ProcLiLaException(ParseTree node, Exception e) {
		super(e);
		this.node = node;
	}
	
}
