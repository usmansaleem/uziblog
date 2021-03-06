package info.usmans.blog;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Creates sitemap for our domain
 *
 * @author Usman Saleem
 */
@WebServlet(name = "sitemap", urlPatterns = {"/sitemap.xml"})
public class SiteMap extends HttpServlet {

    private static final String DEFAULT_HOST = "http://www.usmans.info/";

    @Inject
    private BlogItemSessionEJB _blogSessionEJB;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/xml;charset=UTF-8");
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xmlWriter = new XMLWriter(out, format);
            xmlWriter.write(createSiteMap(req));
            xmlWriter.close();
        } finally {
            out.flush();
            out.close();
        }

    }

    private Document createSiteMap(HttpServletRequest req) {
        Document document = DocumentHelper.createDocument();
        document.addProcessingInstruction("xml-stylesheet", "type='text/xsl' href='resources/css/sitemap.xsl'");

        Element urlset = document.addElement("urlset", "http://www.sitemaps.org/schemas/sitemap/0.9");

        //top level links
        Element url = urlset.addElement("url");
        {
            url.addElement("loc").setText(getOurHost(req));
            url.addElement("changefreq").setText("weekly");
            url.addElement("priority").setText("0.8");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText(getOurHost(req)+"index.xhtml");
            url.addElement("changefreq").setText("weekly");
            url.addElement("priority").setText("0.8");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText(getOurHost(req)+"usman/saleem/about/");
            url.addElement("changefreq").setText("yearly");
            url.addElement("priority").setText("0.9");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText(getOurHost(req)+"usman/saleem/archives/");
            url.addElement("changefreq").setText("monthly");
            url.addElement("priority").setText("0.5");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText(getOurHost(req)+"usman/saleem/notes/");
            url.addElement("changefreq").setText("yearly");
            url.addElement("priority").setText("0.5");
        }

        //create url list of all blog entries
        List<BlogEntry> mainBlogEntriesIdList = null;
        try {
            mainBlogEntriesIdList = _blogSessionEJB.getMainBlogEntriesIdList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (mainBlogEntriesIdList != null)
            for (BlogEntry entry : mainBlogEntriesIdList) {
                url = urlset.addElement("url");
                {
                    url.addElement("loc").setText(getOurHost(req)+"usman/saleem/blog/" + entry.getId());
                    url.addElement("lastmod").setText(new SimpleDateFormat("yyyy-MM-dd").format(entry.getModifiedOn()));
                    url.addElement("changefreq").setText("yearly");
                    url.addElement("priority").setText("0.5");
                }
            }


        return document;
    }

    private String getOurHost(HttpServletRequest request)
            {
                URL requestUrl = null;
                try {
                    requestUrl = new URL(request.getRequestURL().toString());
                } catch (MalformedURLException e) {
                    //nothing to do here.
                }
                if(requestUrl != null) {
                    String portString = requestUrl.getPort() == -1 ? "" : ":" + requestUrl.getPort();
                    return requestUrl.getProtocol() + "://" + requestUrl.getHost() + portString + "/";
                } else {
                    return DEFAULT_HOST;
                }
    }


}
