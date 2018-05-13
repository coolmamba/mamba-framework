package com.mamba.framework.context;

import org.springframework.core.Ordered;

public interface FrameComponentOrdered extends Ordered {
	int CACHE = Ordered.HIGHEST_PRECEDENCE + 100;
	int DCI18N = Ordered.HIGHEST_PRECEDENCE + 101;
	int BEAN_TRANSLATOR = Ordered.HIGHEST_PRECEDENCE + 102;
}
