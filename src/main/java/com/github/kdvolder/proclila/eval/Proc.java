package com.github.kdvolder.proclila.eval;

import com.github.kdvolder.proclila.eval.Computations.Computation;

public interface Proc {

	Computation<Object> apply(Object arg);

}
