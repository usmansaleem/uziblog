package info.usmans.blog.rewrite;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.servlet.config.*;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

import javax.servlet.ServletContext;

/**
 * Rewrite configuration provider using OCPSoft http://www.ocpsoft.org/rewrite/
 * <p/>
 * It is meant to provide friendly URL mapping to blog entries
 *
 * @author Usman Saleem
 */

@RewriteConfiguration
public class RewriteConfigurationProvider extends HttpConfigurationProvider {

    public static final String BLOG_DETAIL_FRIENDLY_PATH = "/usman/saleem/blog/{blogID}";
    public static final String BLOG_ABOUT_FRIENDLY_PATH = "/usman/saleem/about/";
    public static final String BLOG_NOTES_FRIENDLY_PATH = "/usman/saleem/notes/";
    public static final String BLOG_ARCHIVES_FRIENDLY_PATH = "/usman/saleem/archives/";
    public static final String DETAIL_XHTML = "/detail.xhtml";
    public static final String ABOUT_XHTML = "/about.xhtml";
    public static final String NOTES_XHTML = "/notes.xhtml";
    public static final String ARCHIVES_XHTML = "/archive.xhtml";
    public static final String LECTURES_XHTML = "/lectures.xhtml";

    @Override
    public Configuration getConfiguration(ServletContext servletContext) {
        return ConfigurationBuilder.begin()
                // redirect legacy URLs to our new friendly location
                .addRule()
                .when(Direction.isInbound()
                        .and(Path.matches(DETAIL_XHTML).and(Query.parameterExists("blogID"))))
                .perform(Redirect.temporary(servletContext.getContextPath() + BLOG_DETAIL_FRIENDLY_PATH))

                .addRule()
                .when(Direction.isInbound()
                        .and(Path.matches(LECTURES_XHTML)))
                .perform(Redirect.temporary(servletContext.getContextPath() + BLOG_NOTES_FRIENDLY_PATH))

                //redirect blog entries detail page
                .addRule(Join.path(BLOG_DETAIL_FRIENDLY_PATH).to(DETAIL_XHTML))

                //some static rewriting
                .addRule(Join.path(BLOG_ABOUT_FRIENDLY_PATH).to(ABOUT_XHTML))
                .addRule(Join.path(BLOG_NOTES_FRIENDLY_PATH).to(NOTES_XHTML))
                .addRule(Join.path(BLOG_ARCHIVES_FRIENDLY_PATH).to(ARCHIVES_XHTML))
                ;

    }

    @Override
    public int priority() {
        return 0;
    }
}
