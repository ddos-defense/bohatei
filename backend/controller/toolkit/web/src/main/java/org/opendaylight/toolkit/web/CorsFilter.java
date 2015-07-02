package org.opendaylight.toolkit.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.opendaylight.toolkit.web.bean.CorsBean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * CORS Filter for easier web application development
 * 
 * @author Andrew Kim
 *
 */
public class CorsFilter implements Filter {
	public static String VALID_METHODS = "DELETE, HEAD, GET, OPTIONS, POST, PUT";
	private static Type CORS_BEAN = new TypeToken<CorsBean>(){}.getType();
	private static Gson gson = new Gson();

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		
		// determine allowed origin
		StringBuffer http = new StringBuffer("http://");
		String allowedOrigin = "localhost";
		StringBuffer path = new StringBuffer(System.getProperty("user.dir"));
		path.append("/cors.json");
		try {
			String corsBean = readFile(path.toString());
			CorsBean bean = gson.fromJson(corsBean, CORS_BEAN);
			allowedOrigin = bean.getAddress();
			http.append(allowedOrigin).append(":8000");
			response.setHeader("Access-Control-Allow-Origin", http.toString());
			
			// everything else
			response.setHeader("Access-Control-Allow-Credentials", "true");
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "3600");
			response.setHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers");
		} catch (IOException e) {
			// just skip cors if no cors.json found
			//System.err.println("Failed to find cors.json in "+path.toString()+" defaulting to localhost");
		}
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}
	
	private String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, Charset.defaultCharset());
	}

}
