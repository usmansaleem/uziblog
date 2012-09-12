package info.usmans.blog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * An Rss feed mode. An rss contains a channel.
 * 
 * @author usman
 * 
 */
public class RssChannel {
	private static final String RSS_VERSION = "2.0";
	private static final String COPYRIGHT = "(c) 2005-2012 Usman Saleem.";
	private static final String WEBMASTER = "usman@usmans.info (Usman Saleem)";
	private static final String GENERATOR = "RssChannel Generator by Usman Saleem";
	private static final String DOCS = "http://cyber.law.harvard.edu/rss/rss.html";
	private Document rssDocument;

	// Required element
	private Element rssRoot;
	private Element rssChannel;

	public static final SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat(
			"EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	/**
	 * Non Default RssChannel constructor. Call addItem to add further items.
	 * 
	 * @param title
	 * @param link
	 * @param description
	 * @param categories
	 */
	public RssChannel(String title, String link, String description,
			List<String> categories) {
		// create document
		rssDocument = DocumentHelper.createDocument();
		rssRoot = rssDocument.addElement("rss");
		rssRoot.addAttribute("version", RSS_VERSION);

		// add single rss channel element
		rssChannel = rssRoot.addElement("channel");

		// add required channel elements
		rssChannel.addElement("title").addText(title);
		rssChannel.addElement("link").addText(link);
		rssChannel.addElement("description").addText(description);

		// optional channel elements
		rssChannel.addElement("copyright").addText(COPYRIGHT);
		rssChannel.addElement("managingEditor").addText(WEBMASTER);
		rssChannel.addElement("webMaster").addText(WEBMASTER);
		// add pubdate when this object gets created.
		rssChannel.addElement("pubDate").addText(
				RFC822DATEFORMAT.format(new Date()));

		// add categories
		if (categories != null) {
			for (String category : categories) {
				rssChannel.addElement("category").addText(category);
			}
		}

		rssChannel.addElement("generator").addText(GENERATOR);
		rssChannel.addElement("docs").addText(DOCS);

	}

	/**
	 * Add an item to the channel.
	 * 
	 * @param title
	 *            The title of the item
	 * @param link
	 *            The URL of the link.
	 * @param description
	 *            The description of the item
	 * @param author
	 *            Email address of the author
	 * @param categories
	 *            List<String> of categories of this item
	 * @param comments
	 *            URL to the comments. Will not be added if null.
	 * @param guid
	 *            The unique guid. Usually the url to the post.
	 * @param pubDate
	 *            The publication date
	 */
	public void addItem(String title, String link, String description,
			String author, List<String> categories, String comments,
			String guid, Date pubDate) {
		Element rssItem = rssChannel.addElement("item");
		rssItem.addElement("title").addText(title);
		rssItem.addElement("link").addText(link);
		rssItem.addElement("description").addCDATA(description);
		if (author == null || author.trim().length() == 0) {
			author = "usman@usmans.info (Usman Saleem)";
		}
		rssItem.addElement("author").addText(author);
		if (categories != null) {
			for (String category : categories) {
				rssItem.addElement("category").addText(category);
			}
		}
		if (comments != null) {
			rssItem.addElement("comments").addText(comments);
		}
		if (guid != null) {
			rssItem.addElement("guid").addText(guid);
		}
		if (pubDate != null) {
			rssItem.addElement("pubDate").addText(
					RFC822DATEFORMAT.format(pubDate));
		}

	}

	/**
	 * Returns XML as String
	 */
	public String toString() {
		return this.rssDocument.asXML();
	}

}
