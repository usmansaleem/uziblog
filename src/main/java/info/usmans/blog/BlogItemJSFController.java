package info.usmans.blog;

import java.io.Serializable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * This class is the main backing bean for the JSF front end.
 * 
 * @author uzi
 * 
 */
@Named("blogcontroller")
@SessionScoped
public class BlogItemJSFController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1393772424698635004L;

        protected static final String DEFAULT_BLOG_SECTION = "Main";

	/**
         * The main EJB containing our logic
         */    
        @Inject
	private BlogItemSessionEJB _blogfacade;

        /**
         * A singelton EJB acting as a local cache
         */ 
	@Inject
	private BlogEntryGlobal _blogEntryGlobal;

	private LazyDataModel<BlogEntry> lazyModel;

       /**
 	* The default blog section.
 	*/ 
        private String blogSection = DEFAULT_BLOG_SECTION;

	/**
	 * The current select category
	 */
	private long catID;
	private String catName = "Unknown";

	/**
	 * Current selected blog - used when user click on header of a blog entry
	 */
	private long blogID;

	/**
	 * Used when no relevant blog entry is found
	 */
	private BlogEntry selectedBlogEntry;

	/**
	 * Represents new blog. We could have used selectedBlogEntry here as well,
	 * but this will allow us to keep these two separate.
	 */
	private BlogEntry newBlog;

	/**
	 * Default Constructor.
	 */
	public BlogItemJSFController() {
		this.newBlog = new BlogEntry();
		this.newBlog.setTitle("Title");
		this.newBlog.setBody("Body");
		this.newBlog.setCategories(new Long[0]);
	}

	/**
	 * Initialize the lazy loading list to 'default'.
	 * TODO: In addition, we need to initialize it again when blog section change.
	 */
	@PostConstruct
	@SuppressWarnings("serial")
	public void init() {
		lazyModel = new LazyDataModel<BlogEntry>() {
			@Override
			public List<BlogEntry> load(int first, int pageSize,
					String sortField, SortOrder sortOrder,
					Map<String, Object> filters) {
				try {
					// TODO: Obtain blog entry count from infinispan cache
					setRowCount(_blogEntryGlobal.getBlogEntryCount(blogSection));
					return _blogfacade.getBlogEntries(first, pageSize, blogSection);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
			}

		};
		lazyModel.setRowCount(_blogEntryGlobal.getBlogEntryCount(blogSection));

	}

	/**
	 * Return the lazy model for the main blog table
	 * 
	 * @return
	 */
	public LazyDataModel<BlogEntry> getLazyModel() {

		return lazyModel;
	}

	/**
	 * Return blog entries specific to a category
	 * 
	 * @return
	 */
	public List<BlogEntry> getBlogEntriesByCategory() {

		// now return all blog entries by category
		try {
			return _blogfacade.getBlogEntriesByCategory(this.catID, blogSection);
		} catch (SQLException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Database Error", e.getLocalizedMessage()));
			return null;
		}

	}

	/**
	 * Called when user clicks on one of the category link.
	 * 
	 * @param id
	 */
	public void setCatID(long id) {
		// valid category
		boolean validCategory = false;
		Map<Long, String> catMap = _blogEntryGlobal.getCategoryByIdMap();
		if (catMap.containsKey(new Long(id))) {
			validCategory = true;
			this.catID = id;
			this.catName = catMap.get(new Long(id));
		}

		if (!validCategory) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Invalid Category Error",
							"Not a valid category in system."));
			this.catID = 0;
			this.catName = "Unknown";

		}

	}

	public long getCatID() {
		return this.catID;
	}

	/**
	 * Return category name of currently selected category
	 * 
	 * @return
	 */
	public String getCatName() {
		return this.catName;
	}

        /**
 	* Returns currently selected blog section name
 	*/  
        public String getBlogSection() {
		return this.blogSection;
        }

        /**
 	 * Select different blog section and update lazy data table
         */	 
	public void setBlogSection(String section) {
		//determine invalid section and reverts to 'default' 
		if(section != null && section.trim().length() > 0) {
			if(_blogEntryGlobal.isValidBlogSection(section)) {
				//only update section if it is not already selected.
				if(!section.equals(blogSection)) {
					blogSection = section;
					init();
				}
			} else {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Invalid section", "Invalid section passed: " + section));

			}
                } else {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Invalid section", "Invalid (empty or null) section passed"));

		}
        }

	/**
	 * Return currently selected blog entry id
	 * 
	 * @return
	 */
	public long getBlogID() {
		return blogID;
	}

       /**
 	* This method is called when user request a specific blog entry.
 	*/ 	 
	public void setBlogID(long blogID) {
		this.blogID = blogID;

		try {
			this.selectedBlogEntry = _blogfacade.getBlogEntryById(this.blogID);
		} catch (SQLException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Database Error", e.getMessage()));
		}

	}

   

	/**
	 * Return currently selected blog entry
	 * 
	 * @return
	 */
	public BlogEntry getBlog() {
		return this.selectedBlogEntry;

	}

	/**
	 * Return new blog.
	 */
	public BlogEntry getNewBlog() {
		return this.newBlog;
	}

	/**
	 * Save selected blog
	 */
	public String saveBlog() {
		try {
			_blogfacade.updateBlog(this.selectedBlogEntry);
                        //update RSS feed to cater for new changes.
                        _blogEntryGlobal.updateRSSFeed();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Blog Updated", "Blog Updated"));
		} catch (SQLException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance()
					.addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"Database Error while saving blog", e
											.getMessage()));
		} catch(IOException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance()
					.addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									"IOException while updating blog RSS feed", e
											.getMessage()));
                }

		return "/detail.xhtml?blogID="+this.selectedBlogEntry.getId() + "&faces-redirect=true";
	}

	/**
	 * Save new blog entry
	 */
	public void createBlog() {
		if (newBlog == null) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Null newBlog ", "Invalid application state"));
			return;
		}
		try {
			//set blog section (or should we move it to web page (new.xhtml) as well?
			newBlog.setBlogSection(this.blogSection);
			_blogfacade.createBlog(newBlog);
			//update data table to fetch the latest entry
			init();
			// update count in "global" cache
			_blogEntryGlobal.updateBlogEntryCount();

			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"New Blog Entry", "Created!"));
		} catch (SQLException e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Database Error while creating blog", e
							.getMessage()));
		}
	}

	/**
	 * Add a comment event handler
	 */
	public void addComment(ActionEvent e) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
				"Correct",
				"You proved to be a human, but its still work in progress.");
		// TODO Add actual comment
		FacesContext.getCurrentInstance().addMessage(null, msg);

	}

	public void insertCodeBlock() {
		if(newBlog != null) {
			StringBuilder sb = new StringBuilder(newBlog.getBody());
			sb.append("<pre><code data-language=\"bash\"> </code></pre>");
			newBlog.setBody(sb.toString());
		}
	}

	public String logout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "/index.xhtml?faces-redirect=true";
	}
}
