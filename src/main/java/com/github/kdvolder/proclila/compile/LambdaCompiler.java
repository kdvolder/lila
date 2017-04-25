package com.github.kdvolder.proclila.compile;

import org.antlr.v4.runtime.tree.RuleNode;

import com.github.kdvolder.proclila.ProcLiLaBaseVisitor;
import com.github.kdvolder.proclila.ProcLiLaParser;
import com.github.kdvolder.proclila.eval.Computations.Computation;
import com.github.kdvolder.proclila.eval.Environment;
import com.github.kdvolder.proclila.type.Type;

public class LambdaCompiler {

	public class Compiled {
		public final Type type;
		public final Computation<Object> runner;
		
		public Compiled(Type type, Computation<Object> runner) {
			super();
			this.type = type;
			this.runner = runner;
		}
	}

	public interface Compilation {
		Compiled compile(Environment<Type> cte);
	}

	public class Compiler extends ProcLiLaBaseVisitor<Compilation> {

		public final ProcLiLaParser parser;

		public Compiler(ProcLiLaParser parser) {
			this.parser = parser;
		}
		
		@Override
		public Compilation visitChildren(RuleNode node) {
			throw new IllegalStateException("Not implemented compile for: "+node.toStringTree(parser));
		}
	}

	
	
	
//	body
//	: (def ';')* expr
//	;
//
//def	: ID ':' expr
//	;
//
//expr 
//	: expr expr						# apply
//	| expr OP expr					# binOp
//	| 'if' expr '{' body '}' ( 'else' '{' body '}')? 	# if
//	| ID '->' expr					# lambda
//	| ID							# var
//	| STRING						# string
//	| INT							# int
//	| '{' body '}'					# block
//	| '(' expr ')'					# parens
//	| '(' expr ( ',' expr )+ ')'	# tuple
//	;


}
