package info.usmans.blog;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import viecili.jrss.generator.RSSFeedGenerator;
import viecili.jrss.generator.RSSFeedGeneratorFactory;
import viecili.jrss.generator.elem.Channel;
import viecili.jrss.generator.elem.Item;
import viecili.jrss.generator.elem.RSS;

/**
 * The purpose of this class is to avoid extra calls to database for count
 * queries and categories list. The classes which require count will call
 * methods from this class. The classes which insert/update/delete blog entry,
 * will update the count in this class.
 * 
 * @author uzi
 * 
 */
@Singleton
@Named("blogentryglobal")
@Default
public class BlogEntryGlobal implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6685044061281980471L;

	@Inject
	private BlogItemSessionEJB _blogfacade;

	private int blogEntryCount = 0;
	private Map<String, Long> categoriesMap = null;
	private Map<Long, String> categoryNameByIdMap = null;
	private List<Category> categories = null;

	private String globalRSSFeed;
	private Map<Long, String> rssFeedByCat = new TreeMap<Long, String>();

	public BlogEntryGlobal() {
	}

	@PostConstruct
	public void init() {
		updateCategories();
		updateBlogEntryCount();

	}

	/**
	 * Used by jsf controller beans to get total number of blog entries rather
	 * than to hit database on every call.
	 * 
	 * @return
	 */
	public int getBlogEntryCount() {
		return blogEntryCount;
	}

	/**
	 * Should be call after insert/update/delete of blog entry.
	 */
	public void updateBlogEntryCount() {
		try {
			blogEntryCount = _blogfacade.getCount();
			updateRSSFeed();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void updateRSSFeed() throws SQLException, IOException {
		// update global RSS feed and channel specific RSS feed
		RSSFeedGenerator rssGenerator = RSSFeedGeneratorFactory.getDefault();
		Channel channel = new Channel("Usman Saleem - Blog",
				"http://usmans.info/",
				"Usman Saleem Blog for Java, Linux and other stuff.");
		channel.setCopyright("(c) 2005-2011 Usman Saleem.");
		channel.setManagingEditor("usman@usmans.info");
		channel.setWebMaster("usman@usmans.info");
		// iterate through global categories
		for (Category cat : getCategories()) {
			channel.addCategory(channel.new Category(cat.getName()));
		}

		// iterate through blog entries
		for (BlogEntry blog : _blogfacade.getTopTenBlogEntries()) {
			Item item = new Item(blog.getTitle(), blog.getBody());
			item.setAuthor("Uzi");
			item.setLink("http://usmans.info/detail.xhtml?blogID="
					+ blog.getId());
			item.setGuid(item.new Guid("blogId=" + blog.getId()));
			item.setPubDate(blog.getCreatedon());
			for (Long catID : blog.getCategories()) {
				item.addCategory(item.new Category(categoryNameByIdMap
						.get(catID)));
			}

			channel.addItem(item);
		}

		RSS rss = new RSS();
		rss.addChannel(channel);

		this.globalRSSFeed = rssGenerator.generateAsString(rss);
		updateRSSFeedByCat();
	}

	private void updateRSSFeedByCat() throws SQLException, IOException {
		// iterate through global categories
		for (Category cat : getCategories()) {

			// update global RSS feed and channel specific RSS feed
			RSSFeedGenerator rssGenerator = RSSFeedGeneratorFactory
					.getDefault();
			Channel channel = new Channel("Usman Saleem - Blog ["
					+ cat.getName() + "]", "http://usmans.info/",
					"Usman Saleem Blog for Java, Linux and other stuff.");
			channel.setCopyright("(c) 2005-2011 Usman Saleem.");
			channel.setManagingEditor("usman@usmans.info");
			channel.setWebMaster("usman@usmans.info");
			for (Category _cat : getCategories()) {
				channel.addCategory(channel.new Category(_cat.getName()));
			}

			// iterate through blog entries
			for (BlogEntry blog : _blogfacade.getTop10BlogEntriesByCategory(cat
					.getId())) {
				Item item = new Item(blog.getTitle(), blog.getBody());
				item.setAuthor("Uzi");
				item.setLink("http://usmans.info/detail.xhtml?blogID="
						+ blog.getId());
				item.setGuid(item.new Guid("blogId=" + blog.getId()));
				for (Long catID : blog.getCategories()) {
					item.addCategory(item.new Category(categoryNameByIdMap
							.get(catID)));
				}

				channel.addItem(item);
			}

			RSS rss = new RSS();
			rss.addChannel(channel);
			this.rssFeedByCat.put(cat.getId(),
					rssGenerator.generateAsString(rss));

		}

	}

	public Map<String, Long> getCategoriesMap() {
		return categoriesMap;
	}

	public Map<Long, String> getCategoryByIdMap() {
		return categoryNameByIdMap;
	}

	public List<Category> getCategories() {
		return categories;
	}

	/**
	 * Should be call when a new category is inserted or an existing one is
	 * updated/deleted.
	 */
	public void updateCategories() {
		try {
			categoriesMap = _blogfacade.getCategoriesMap();
			// fill categories list
			categories = new ArrayList<Category>();
			categoryNameByIdMap = new TreeMap<Long, String>();
			Set<Entry<String, Long>> _set = categoriesMap.entrySet();
			for (Entry<String, Long> entry : _set) {
				Category cat = new Category();
				cat.setName(entry.getKey());
				cat.setId(entry.getValue());
				categories.add(cat);

				categoryNameByIdMap.put(cat.getId(), cat.getName());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getGlobalRSSFeed() {
		return globalRSSFeed;
	}

	public String getRSSFeedByCat(Long id) {
		return this.rssFeedByCat.get(id);
	}

	public String getServerInfo() {
		return ((ServletContext) FacesContext.getCurrentInstance()
				.getExternalContext().getContext()).getServerInfo();
	}

	public String getJsfTitle() {
		return FacesContext.class.getPackage().getImplementationTitle();
	}

	public String getJsfVersion() {
		return FacesContext.class.getPackage().getImplementationVersion();
	}

}
