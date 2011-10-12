package info.usmans.blog;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RssServlet
 */
@WebServlet(description = "RssServlet", urlPatterns = { "/rss" })
public class RssServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private BlogEntryGlobal _blogEntryGlobal;
   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RssServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/xml");
		PrintWriter pw = response.getWriter();
		String output = "";
		String catIDParam = request.getParameter("catID");
		if(catIDParam != null && catIDParam.length() > 0) {
			try {
				Long catID = new Long(catIDParam);
				if(_blogEntryGlobal.getCategoryByIdMap().containsKey(catID)) {
					output = _blogEntryGlobal.getRSSFeedByCat(catID);
				}else {
					output = _blogEntryGlobal.getGlobalRSSFeed();
				}
			}catch(NumberFormatException e) {
				output = _blogEntryGlobal.getGlobalRSSFeed();
			}
		}else {
			output = _blogEntryGlobal.getGlobalRSSFeed();
		}
		pw.println(output);
		pw.flush();
		pw.close();
	}

}
