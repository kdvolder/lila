package com.github.kdvolder.proclila.eval;

import com.github.kdvolder.proclila.ProcLiLaParser.ExprContext;
import com.github.kdvolder.proclila.eval.LambdaInterp.Evaluator;

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
