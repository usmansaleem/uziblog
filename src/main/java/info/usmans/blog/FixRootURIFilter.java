import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

/**
* This filter is to fix an invonvinence where 
* JSF link tag's 'outcome' is not calculated correctly
* if application is accessed with just the domain name
* i.e http://www.usmans.info .
* 
* We forward the request to index.xhtml if the request URI is just /. 
* This is probably just JBoss/Mojarra issue, but we needed this fix
* to avoid having index.jsp which redirect to index.xhtml
*
* @author Usman Saleem
* @version 1.0
*/
@WebFilter(urlPatterns={"/*"})
public class FixRootURIFilter implements Filter {

    private FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
	if(request instanceof HttpServletRequest) {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		String uri = httpReq.getRequestURI();
		if(uri != null && uri.equals("/")) {
			RequestDispatcher rd = filterConfig.getServletContext().getRequestDispatcher("/index.xhtml");
			if(rd != null) {
				rd.forward(request, response);
				return;
			}
		}
	}

	//default handling - do nothing and forward reqeust to filter chain
        chain.doFilter(request, response);

    }
    @Override
    public void destroy() {    }
}


