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
import java.util.List;

/**
 * Creates sitemap for usmans.info
 * @author Usman Saleem
 */
@WebServlet(name = "sitemap", urlPatterns = {"/sitemap.xml"})
public class SiteMap extends HttpServlet {

    @Inject
    private BlogItemSessionEJB _blogSessionEJB;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/xml;charset=UTF-8");
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xmlWriter = new XMLWriter(out, format);
            xmlWriter.write(createSiteMap());
            xmlWriter.close();
        }finally {
            out.flush();
            out.close();
        }

    }

    Document createSiteMap() {
        Document document = DocumentHelper.createDocument();
        Element urlset = document.addElement("urlset", "http://www.sitemaps.org/schemas/sitemap/0.9");

        //top level links
        Element url = urlset.addElement("url");
        {
            url.addElement("loc").setText("http://www.usmans.info/");
            url.addElement("changefreq").setText("weekly");
            url.addElement("priority").setText("0.8");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText("http://www.usmans.info/index.xhtml");
            url.addElement("changefreq").setText("weekly");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText("http://www.usmans.info/about.xhtml");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText("http://www.usmans.info/archive.xhtml");
        }

        url = urlset.addElement("url");
        {
            url.addElement("loc").setText("http://www.usmans.info/notes.xhtml");
        }

        //create url list of all blog entries
        List<Integer> mainBlogEntriesIdList = _blogSessionEJB.getMainBlogEntriesIdList();
        for (Integer id : mainBlogEntriesIdList) {
            url = urlset.addElement("url");
            {
                url.addElement("loc").setText("http://www.usmans.info/detail.xhtml?blogID=" + id);
            }
        }


        return document;
    }


}
