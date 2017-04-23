package com.github.kdvolder.proclila;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.kdvolder.proclila.Computations.Computation;
import com.github.kdvolder.proclila.LambdaInterp.Evaluator;
import com.github.kdvolder.proclila.ProcLiLaParser.ExprContext;

public class EvaluatingProc implements Proc {

	private final Environment lexicalEnv;
	private final TerminalNode id;
	private final ExprContext expr;
	private final Evaluator eval;
	private final Computation<Object> body;

	public EvaluatingProc(Evaluator eval, Environment env, TerminalNode id, ExprContext expr) {
		this.eval = eval;
		this.lexicalEnv = env;
		this.id = id;
		this.expr = expr;
		this.body = expr.accept(eval);
	}
	
	@Override
	public Computation<Object> apply(Object arg) {
		Environment bodyEnv = this.lexicalEnv.extend();
		return bodyEnv.def(id, arg)
		.then(ignore -> body.useEnv(bodyEnv));
	}

	@Override
	public String toString() {
		return "(" + id + " -> " + expr.toStringTree(eval.parser) + ")";
	}
}
