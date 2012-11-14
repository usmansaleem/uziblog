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

/**
 * The purpose of this class is to avoid extra calls to database for count
 * queries and categories list. The classes which require count will call
 * methods from this class. The classes which insert/update/delete blog entry,
 * will update the count in this class.
 * 
 * This class is declared as a singelton, and it will provide some statistical
 * information to be brag about on site.
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
	
	private String databaseInfo = "Unknown";
	private String serverInfo;
	private String jsfTitle;
	private String jsfVersion;

	public BlogEntryGlobal() {
	}

	@PostConstruct
	public void init() {
		updateDatabaseInfo();
		updateServerInfo();
		updateJSFTitle();
		updateJSFVersion();
		
		updateCategories(); //TODO: Move
		updateBlogEntryCount(); //TODO: Move

	}

	/**
	 * Update database product information at this singelton initialization
	 */
	private void updateDatabaseInfo() {
		databaseInfo = _blogfacade.getDatabaseInfo();

	}

	/**
	 * Update Server Information
	 */
	private void updateServerInfo() {
		this.serverInfo = ((ServletContext) FacesContext.getCurrentInstance()
				.getExternalContext().getContext()).getServerInfo();
	}

	/**
	 * Update JSF implementation title.
	 */
	private void updateJSFTitle() {
		this.jsfTitle = FacesContext.class.getPackage()
				.getImplementationTitle();
	}

	private void updateJSFVersion() {
		this.jsfVersion = FacesContext.class.getPackage().getImplementationVersion();
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

	/**
	 * Update RSS Feed local cache.
	 * 
	 * @param Category
	 *            . Category specific RSS to update. null will update main RSS
	 *            feed
	 */
	private void updateRSSFeed(Category cat) throws SQLException, IOException {
		String title = "Usman Saleem - Blog";
		List<BlogEntry> blogEntryList = null;

		if (cat != null) {
			title = title + " - " + cat.getName();
			blogEntryList = _blogfacade.getTopTenBlogEntriesByCategory(cat
					.getId());
		} else {
			blogEntryList = _blogfacade.getTopTenBlogEntries();
		}
		// update global RSS feed and channel specific RSS feed

		// iterate through global categories and create a List<String>
		List<String> categoriesList = new ArrayList<String>();
		for (Category _cat : getCategories()) {
			categoriesList.add(_cat.getName());
		}

		RssChannel rssChannel = new RssChannel(title, "http://www.usmans.info",
				"Usman Saleem Blog for Java, Linux and other stuff.",
				categoriesList);

		// iterate through blog entries
		for (BlogEntry blog : blogEntryList) {
			List<String> itemCategoryList = new ArrayList<String>();
			for (Long catID : blog.getCategories()) {
				itemCategoryList.add(categoryNameByIdMap.get(catID));
			}
			rssChannel.addItem(blog.getTitle(),
					"http://usmans.info/detail.xhtml?blogID=" + blog.getId(),
					blog.getBody(), "Uzi", itemCategoryList, null, "blogId="
							+ blog.getId(), blog.getCreatedon());

		}

		if (cat != null) {
			this.rssFeedByCat.put(cat.getId(), rssChannel.toString());
		} else {
			this.globalRSSFeed = rssChannel.toString();
		}

	}

	public void updateRSSFeed() throws SQLException, IOException {
		updateRSSFeed(null);
		// iterate through global categories
		for (Category cat : getCategories()) {
			updateRSSFeed(cat);
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

	/**
	 * Used by RssServlet, TODO: deprecate its usage
	 * @return
	 */
	public String getGlobalRSSFeed() {
		return globalRSSFeed;
	}

	/**
	 * Used by RssServlet, TODO deprecate its usage!
	 * @param id
	 * @return
	 */
	public String getRSSFeedByCat(Long id) {
		return this.rssFeedByCat.get(id);
	}

	/**
	 * Returns Server Information
	 * 
	 * @return
	 */
	public String getServerInfo() {
		return this.serverInfo;
	}

	/**
	 * Returns JSF Implementation
	 * 
	 * @return
	 */
	public String getJsfTitle() {
		return this.jsfTitle;
	}

	/**
	 * Returns JSF Implementation version
	 * @return
	 */
	public String getJsfVersion() {
		return this.jsfVersion;
	}

	/**
	 * Returns JDBC Database Vendor
	 * @return
	 */
	public String getDatabaseInfo() {
		return databaseInfo;
	}

}
