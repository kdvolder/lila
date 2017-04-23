package com.github.kdvolder.proclila.eval;

import java.math.BigInteger;
import java.util.Optional;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.kdvolder.proclila.ProcLiLaBaseVisitor;
import com.github.kdvolder.proclila.ProcLiLaLexer;
import com.github.kdvolder.proclila.ProcLiLaParser;
import com.github.kdvolder.proclila.ProcLiLaParser.ApplyContext;
import com.github.kdvolder.proclila.ProcLiLaParser.BinOpContext;
import com.github.kdvolder.proclila.ProcLiLaParser.BlockContext;
import com.github.kdvolder.proclila.ProcLiLaParser.BodyContext;
import com.github.kdvolder.proclila.ProcLiLaParser.DefContext;
import com.github.kdvolder.proclila.ProcLiLaParser.ExprContext;
import com.github.kdvolder.proclila.ProcLiLaParser.IfContext;
import com.github.kdvolder.proclila.ProcLiLaParser.IntContext;
import com.github.kdvolder.proclila.ProcLiLaParser.LambdaContext;
import com.github.kdvolder.proclila.ProcLiLaParser.ParensContext;
import com.github.kdvolder.proclila.ProcLiLaParser.VarContext;
import com.github.kdvolder.proclila.eval.Computations.Computation;
import com.github.kdvolder.proclila.util.Functions;
import com.github.kdvolder.proclila.util.ProcLiLaException;

public class LambdaInterp {

	private Computations c = new DefaultComputations();
	
	public class Evaluator extends ProcLiLaBaseVisitor<Computation<Object>> {

		public final ProcLiLaParser parser;

		public Evaluator(ProcLiLaParser parser) {
			this.parser = parser;
		}
		
		@Override
		public Computation<Object> visitChildren(RuleNode node) {
			throw new IllegalStateException("Not implemented eval for: "+node.toStringTree(parser));
		}

// 		| expr expr					# apply
		@Override
		public Computation<Object> visitApply(ApplyContext ctx) {
			return trace(ctx,
				ctx.expr(0).accept(this)
				.then(proc -> ctx.expr(1).accept(this)
				.then(arg -> applyProc(proc, arg, ctx)))
			);
		}
		
		private Computation<Object> applyProc(Object proc, Object arg, ExprContext errorCtx) {
			if (proc instanceof Proc) {
				return ((Proc)proc).apply(arg);
			}
			return c.fail(new ProcLiLaException(errorCtx, "Can not apply, not a procedure: "+proc));
		}
		
//		| expr OP expr						# binOp
		@Override
		public Computation<Object> visitBinOp(BinOpContext ctx) {
			ExprContext el = ctx.expr(0);
			ExprContext er = ctx.expr(1);
			TerminalNode op = ctx.OP();
			Computation<Object> cl = el.accept(this);
			Computation<Object> cr = er.accept(this);
			Computation<Object> co = c.withEnv(env -> env.get(op));
			return co.and(cl)
			.then(Functions.fn2((o,  l) -> applyProc(o, l, el).and(cr)))
			.then(Functions.fn2((pl, r) -> applyProc(pl, r, er)));
		}

//		| ID '->' expr				# lambda
		@Override
		public Computation<Object> visitLambda(LambdaContext ctx) {
			return trace(ctx, 
				c.withEnv(env -> 
					c.just(new EvaluatingProc(this, env, ctx.ID(), ctx.expr()))
				)
			);
		}
		
//TODO:	| STRING					# string
//		| INT						# int
		@Override
		public Computation<Object> visitInt(IntContext ctx) {
			String str = ctx.getText();
			return trace(ctx, c.just(new BigInteger(str)));
		}

//		| ID								# var
		@Override
		public Computation<Object> visitVar(VarContext ctx) {
			return trace(ctx, c.withEnv(env -> env.get(ctx.ID())));
		}
		
//		| ID ':' expr				# def
		@Override
		public Computation<Object> visitDef(DefContext ctx) {
			Computation<Object> cval = ctx.expr().accept(this);
			return  cval.then(value -> 
				c.withEnv(env -> env.def(ctx.ID(), value))
			);
		}
		
//		| '(' expr ')'				# parens
		@Override
		public Computation<Object> visitParens(ParensContext ctx) {
			return trace(ctx, ctx.expr().accept(this));
		}
		
//TODO:	| '(' expr ( ',' expr )+ ')'		# tuple

//		| 'if' expr '{' body '}' ( 'else' '{' expr '}')? 	# if
		@Override
		public Computation<Object> visitIf(IfContext ctx) {
			Computation<Object> cnd = ctx.expr().accept(this);
			Computation<Object> thn = ctx.body(0).accept(this);
			BodyContext _els = ctx.body(1);
			Computation<Object> els = _els==null ? c.just(null) : _els.accept(this);
			return trace(ctx, 
				cnd.then((test) -> isTruthy(test) 
					? thn
					: els
				)
			);
		}
		
//		| '{' expr '}'					# block
		@Override
		public Computation<Object> visitBlock(BlockContext ctx) {
			return ctx.body().accept(this);
		}

//		body : (def ';')* expr
		@Override
		public Computation<Object> visitBody(BodyContext ctx) {
			Optional<Computation<Object>> defs = ctx.def().stream()
					.map(def -> def.accept(this))
					.reduce((d1, d2) -> d1.then(ignore -> d2));
			Computation<Object> expr = ctx.expr().accept(this);
			return defs.isPresent() 
				? defs.get().then(ignore -> expr)
				: expr;
		}

		private boolean isTruthy(Object test) {
			return !(test instanceof Boolean) || (Boolean)test;
		}

		public <X> Computation<X> trace(ExprContext ctx, Computation<X> body) {
			if (evalListener!=null) {
				return c.fromRunnable(() -> evalListener.enter(this, ctx))
				.then(ignore -> body)
				.then(
					(value) -> {
						evalListener.exit(this, ctx, value, null);
						return c.just(value);
					},
					(e) -> { 
						evalListener.exit(this, ctx, null, e);
						return c.fail(e);
					}
				);
			}
			return body;
		}

	}

	private EvalListener evalListener = null;

	public Object eval(String string) throws Exception {
		CharStream charstream = new ANTLRInputStream(string);
		ProcLiLaLexer lexer = new ProcLiLaLexer(charstream);
		TokenStream tokens = new CommonTokenStream(lexer);
		ProcLiLaParser parser = new ProcLiLaParser(tokens);
		BodyContext expr = parser.body();
		
		System.out.println(expr.toStringTree(parser));
		
		return c.run(expr.accept(new Evaluator(parser)));
	}


	public LambdaInterp addListener(EvalListener evalListener) {
		this.evalListener = evalListener;
		return this;
	}
	
//	@Override
//	public Object visitInt(IntContext ctx) {
//		return new BigInteger(ctx.getText());
//	}
}
