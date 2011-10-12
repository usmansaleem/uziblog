package info.usmans.blog;

import java.io.Serializable;

/**
 * Represents a category.
 * @author uzi
 *
 */
public class Category implements Serializable {
	private static final long serialVersionUID = 1944020798729084699L;
	private long id;
	private String name;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
