package com.github.kdvolder.proclila;

import com.github.kdvolder.proclila.Computations.Computation;

public interface Proc {

	Computation<Object> apply(Object arg);

}
