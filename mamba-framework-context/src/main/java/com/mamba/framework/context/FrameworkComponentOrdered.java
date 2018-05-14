package com.mamba.framework.context;

import org.springframework.core.Ordered;

public interface FrameworkComponentOrdered extends Ordered {
	int CACHE = Ordered.HIGHEST_PRECEDENCE + 100;
	int I18N = Ordered.HIGHEST_PRECEDENCE + 101;
	int SIP = Ordered.HIGHEST_PRECEDENCE + 102;
	int BEAN_TRANSLATOR = Ordered.HIGHEST_PRECEDENCE + 103;
}
