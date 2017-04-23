package com.github.kdvolder.proclila;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import com.github.kdvolder.proclila.LambdaInterp.Evaluator;
import com.github.kdvolder.proclila.ProcLiLaParser.ExprContext;

public class LambdaInterpTest {

	LambdaInterp interp = new LambdaInterp();
	
	@Before 
	public void enableTracing() {
		interp.addListener(new EvalListener() {
			
			int nestingLevel = 0;
	
			@Override
			public void enter(Evaluator eval, ExprContext exp) {
				printIndent();
				System.out.println(">> "+ exp.toStringTree(eval.parser));
				nestingLevel++;
			}
	
			private void printIndent() {
				for (int i = 0; i < nestingLevel; i++) {
					System.out.print("  ");
				}
			}
	
			@Override
			public void exit(Evaluator eval, ExprContext exp, Object value, Throwable e) {
				nestingLevel--;
				printIndent();
				if (e!=null) {
					System.out.println("<< ERROR: " + e.getMessage());
				} else {
					System.out.println("<< "+value);
				}
			}
		});
	}

	
	@Test public void number() throws Exception {
		assertValue(4, "4");
	}
	
	@Test public void variable() throws Exception {
		assertValue(99, 
			"  name: 99 ;\n" +
			"  name\n" 
		);
	}
	
	@Test public void parens() throws Exception {
		assertValue(99, "((99))");
	}

	@Test public void block() throws Exception {
		assertValue(99, "{a: 44; a+55}");
	}

	
	@Test public void lambda() throws Exception {
		assertValue(88, 
				"{\n" +
				"  id : x -> x;\n" + 
				"  id 88\n" +
				"}"
		);
	}
	
	@Test public void factorial() throws Exception {
		assertValue(4*3*2, 
				"   fac : n ->\n" +
				"      if n==1 {\n" +
				"         1\n"+
				"      } else {\n" +
	            "         n * fac(n-1)\n" +
				"      };\n"+
				"   fac 4\n" 
		);
	}
	
	@Test public void equalOp() throws Exception {
		assertValue(false, "2==3");
		assertValue(true, "2==2");
	}

	private void assertValue(boolean b, String expression) throws Exception {
		assertEquals(b, interp.eval(expression));
	}

	private void assertValue(int expectedValue, String expression) throws Exception {
		assertEquals(BigInteger.valueOf(expectedValue), interp.eval(expression));
	}
}
