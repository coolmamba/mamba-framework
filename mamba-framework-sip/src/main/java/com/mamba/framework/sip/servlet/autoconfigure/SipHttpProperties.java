package com.mamba.framework.sip.servlet.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sip")
public class SipHttpProperties {
	private int loadOnStartup = -1;
	private String servletPath = "/";

	public int getLoadOnStartup() {
		return this.loadOnStartup;
	}

	public void setLoadOnStartup(int loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
	}

	public String getServletPath() {
		return this.servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getServletMapping() {
		if (this.servletPath.equals("") || this.servletPath.equals("/")) {
			return "/";
		}
		if (this.servletPath.contains("*")) {
			return this.servletPath;
		}
		if (this.servletPath.endsWith("/")) {
			return this.servletPath + "*";
		}
		return this.servletPath + "/*";
	}

}
