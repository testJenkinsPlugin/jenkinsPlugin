package com.amcbridge.jenkins.plugins.TTS;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

public class TTSServletFilter implements Filter {

	private Filter defaultFilter;

	public TTSServletFilter(Filter pDefaultFilter) {
		this.defaultFilter = pDefaultFilter;
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		this.defaultFilter.init(filterConfig);
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try
		{
			this.defaultFilter.doFilter(request, response, chain);
		}
		catch (Exception ex)
		{
			((HttpServletResponse) response).sendRedirect(StringUtils.EMPTY);
		}
	}

	public void destroy() {
		this.defaultFilter.destroy();
	}

}