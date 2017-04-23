package com.github.kdvolder.proclila;

import com.github.kdvolder.proclila.LambdaInterp.Evaluator;
import com.github.kdvolder.proclila.ProcLiLaParser.ExprContext;

public interface EvalListener {
	
	EvalListener NULL = new EvalListener() {
		@Override
		public void exit(Evaluator eval, ExprContext exp, Object value, Throwable e) {
		}

		@Override
		public void enter(Evaluator eval, ExprContext exp) {
		}
		
	};
	void exit(Evaluator eval, ExprContext exp, Object value, Throwable e);
	void enter(Evaluator eval, ExprContext exp);
	
}
