package com.github.kdvolder.proclila.eval;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.kdvolder.proclila.ProcLiLaParser.ExprContext;
import com.github.kdvolder.proclila.eval.Computations.Computation;
import com.github.kdvolder.proclila.eval.LambdaInterp.Evaluator;

public class EvaluatingProc implements Proc {

	private final Computations<Object> c;
	private final Environment<Object> lexicalEnv;
	private final TerminalNode id;
	private final ExprContext expr;
	private final Evaluator eval;
	private final Computation<Object> body;

	public EvaluatingProc(Computations<Object> c, Evaluator eval, Environment<Object> env, TerminalNode id, ExprContext expr) {
		this.c = c;
		this.eval = eval;
		this.lexicalEnv = env;
		this.id = id;
		this.expr = expr;
		this.body = expr.accept(eval);
	}
	
	@Override
	public Computation<Object> apply(Object arg) {
		Environment<Object> bodyEnv = this.lexicalEnv.extend();
		return bodyEnv.def(id, arg)
		.then(ignore -> c.useEnv(body, bodyEnv));
	}

	@Override
	public String toString() {
		return "(" + id + " -> " + expr.toStringTree(eval.parser) + ")";
	}
}
