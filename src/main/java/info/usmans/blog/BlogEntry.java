package info.usmans.blog;

import java.io.Serializable;
import java.util.Date;

/**
 * Value Object used by JSF front end to display blog entries.
 * @author uzi
 *
 */
public class BlogEntry implements Serializable {
	private static final long serialVersionUID = 2780688450645230256L;

	private long id;
	private String title;
	private String body;
	private String more;
	private Date createdon;
	private Long[] categories;
        private String blogSection = "default";

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getMore() {
		return more;
	}

	public void setMore(String more) {
		this.more = more;
	}

	public Date getCreatedon() {
		return createdon;
	}

	public void setCreatedon(Date createdon) {
		this.createdon = createdon;
	}

	public Long[] getCategories() {
		return categories;
	}

	public void setCategories(Long[] categories) {
		this.categories = categories;
	}
	
	public String getBlogSection() {
		return this.blogSection;
	}

	public void setBlogSection(String blogSection) {
		this.blogSection = blogSection;
	}
	
}
