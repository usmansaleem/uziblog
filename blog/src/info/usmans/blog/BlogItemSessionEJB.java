package info.usmans.blog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

/**
 * Session Bean implementation class BlogItemSessionFacade
 */
@Stateless
@LocalBean
public class BlogItemSessionEJB {

	// select distinct(to_char(itime,'YYYY-MM')) from blog_item;
	/**
	 * The saved DataSource for the database
	 */
	@Inject
	@Named("jdbc/uziblog")
	private DataSource _ds;

	/**
	 * Default constructor.
	 */
	public BlogItemSessionEJB() {
	}

	public int getCount() throws SQLException {
		Connection con = null;
		int count = 0;
		try {
			con = _ds.getConnection();
			PreparedStatement ps = con
					.prepareStatement("select count(*) from blog_item");
			ResultSet rs = ps.executeQuery();
			rs.next();
			count = rs.getInt(1);
			rs.close();
			ps.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}

		return count;
	}

	public List<BlogEntry> getBlogEntries(int offset, int limit)
			throws SQLException {
		Connection con = null;
		ArrayList<BlogEntry> blogEntry = new ArrayList<BlogEntry>();
		try {
			con = _ds.getConnection();
			PreparedStatement ps2 = con
					.prepareStatement("select c.id from blog_categories c, blog_item_categories ic where c.id=ic.category_id AND ic.item_id=?");
			PreparedStatement ps = con
					.prepareStatement("select inumber, ititle, ibody, imore, itime from blog_item order by itime desc LIMIT ? OFFSET ?");
			ps.setInt(2, offset);
			ps.setInt(1, limit);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				BlogEntry e = new BlogEntry();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setBody(rs.getString(3));
				e.setMore(rs.getString(4));
				e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
				// load categories for this blog entry
				ArrayList<Long> catSet = new ArrayList<Long>();
				ps2.clearParameters();
				ps2.setLong(1, e.getId());
				ResultSet rs2 = ps2.executeQuery();
				while (rs2.next()) {
					catSet.add(rs2.getLong(1));
				}
				rs2.close();
				e.setCategories(catSet.toArray(new Long[catSet.size()]));
				blogEntry.add(e);
			}

			rs.close();
			ps2.close();
			ps.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}

		return blogEntry;

	}

	/**
	 * Returns top 10 entries for global RSS feed
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<BlogEntry> getTopTenBlogEntries() throws SQLException {
		Connection con = null;
		ArrayList<BlogEntry> blogEntry = new ArrayList<BlogEntry>();
		try {
			con = _ds.getConnection();
			PreparedStatement ps2 = con
					.prepareStatement("select c.id from blog_categories c, blog_item_categories ic where c.id=ic.category_id AND ic.item_id=?");
			PreparedStatement ps = con
					.prepareStatement("select inumber, ititle, ibody, imore, itime from blog_item order by itime desc LIMIT 10");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				BlogEntry e = new BlogEntry();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setBody(rs.getString(3));
				e.setMore(rs.getString(4));
				e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
				// load categories for this blog entry
				ArrayList<Long> catSet = new ArrayList<Long>();
				ps2.clearParameters();
				ps2.setLong(1, e.getId());
				ResultSet rs2 = ps2.executeQuery();
				while (rs2.next()) {
					catSet.add(rs2.getLong(1));
				}
				rs2.close();
				e.setCategories(catSet.toArray(new Long[catSet.size()]));
				blogEntry.add(e);
			}

			rs.close();
			ps2.close();
			ps.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}

		return blogEntry;

	}

	/**
	 * Return all categories for blog. This should be called once at application
	 * initialization time as the categories are not usually change. This is
	 * designed to be used by selectItems
	 * 
	 * @return
	 */
	public Map<String, Long> getCategoriesMap() throws SQLException {
		Connection con = null;
		Map<String, Long> map = new TreeMap<String, Long>();
		try {
			con = _ds.getConnection();
			PreparedStatement ps = con
					.prepareStatement("select id, name from blog_categories");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString(2), rs.getLong(1));
			}

			rs.close();
			ps.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}

		return map;
	}

	public List<BlogEntry> getBlogEntriesByCategory(long catID)
			throws SQLException {
		Connection con = null;
		ArrayList<BlogEntry> blogEntry = new ArrayList<BlogEntry>();
		try {
			con = _ds.getConnection();
			PreparedStatement ps2 = con
					.prepareStatement("select c.id from blog_categories c, blog_item_categories ic where c.id=ic.category_id AND ic.item_id=?");

			PreparedStatement ps = con
					.prepareStatement("SELECT inumber, ititle, ibody, imore, itime FROM blog_item INNER JOIN blog_item_categories ON inumber = item_id WHERE category_id = ? order by itime desc ");
			ps.setLong(1, catID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				BlogEntry e = new BlogEntry();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setBody(rs.getString(3));
				e.setMore(rs.getString(4));
				e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
				// load categories for this blog entry
				ArrayList<Long> catSet = new ArrayList<Long>();
				ps2.clearParameters();
				ps2.setLong(1, e.getId());
				ResultSet rs2 = ps2.executeQuery();
				while (rs2.next()) {
					catSet.add(rs2.getLong(1));
				}
				rs2.close();
				e.setCategories(catSet.toArray(new Long[catSet.size()]));
				blogEntry.add(e);
			}

			rs.close();
			ps2.close();
			ps.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}

		return blogEntry;
	}

	/**
	 * Designed for RSS feeds
	 * 
	 * @param catID
	 * @return
	 * @throws SQLException
	 */
	public List<BlogEntry> getTop10BlogEntriesByCategory(long catID)
			throws SQLException {
		Connection con = null;
		ArrayList<BlogEntry> blogEntry = new ArrayList<BlogEntry>();
		try {
			con = _ds.getConnection();
			PreparedStatement ps2 = con
					.prepareStatement("select c.id from blog_categories c, blog_item_categories ic where c.id=ic.category_id AND ic.item_id=?");

			PreparedStatement ps = con
					.prepareStatement("SELECT inumber, ititle, ibody, imore, itime FROM blog_item INNER JOIN blog_item_categories ON inumber = item_id WHERE category_id = ? order by itime desc LIMIT 10 ");
			ps.setLong(1, catID);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				BlogEntry e = new BlogEntry();
				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setBody(rs.getString(3));
				e.setMore(rs.getString(4));
				e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
				// load categories for this blog entry
				ArrayList<Long> catSet = new ArrayList<Long>();
				ps2.clearParameters();
				ps2.setLong(1, e.getId());
				ResultSet rs2 = ps2.executeQuery();
				while (rs2.next()) {
					catSet.add(rs2.getLong(1));
				}
				rs2.close();
				e.setCategories(catSet.toArray(new Long[catSet.size()]));
				blogEntry.add(e);
			}

			rs.close();
			ps2.close();
			ps.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}

		return blogEntry;
	}

	public BlogEntry getBlogEntryById(long blogID) throws SQLException {
		BlogEntry e = new BlogEntry();
		e.setId(0);
		e.setTitle("");
		e.setBody("");
		e.setMore("");
		e.setCreatedon(new Date());

		Connection con = null;
		try {
			con = _ds.getConnection();
			PreparedStatement ps2 = con
					.prepareStatement("select c.id from blog_categories c, blog_item_categories ic where c.id=ic.category_id AND ic.item_id=?");

			PreparedStatement ps = con
					.prepareStatement("SELECT inumber, ititle, ibody, imore, itime FROM blog_item WHERE inumber = ? order by itime desc ");
			ps.setLong(1, blogID);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {

				e.setId(rs.getInt(1));
				e.setTitle(rs.getString(2));
				e.setBody(rs.getString(3));
				e.setMore(rs.getString(4));
				e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
				// load categories for this blog entry
				ArrayList<Long> catSet = new ArrayList<Long>();
				ps2.clearParameters();
				ps2.setLong(1, e.getId());
				ResultSet rs2 = ps2.executeQuery();
				while (rs2.next()) {
					catSet.add(rs2.getLong(1));
				}
				rs2.close();
				e.setCategories(catSet.toArray(new Long[catSet.size()]));
			}

			rs.close();
			ps2.close();
			ps.close();
		} finally {
			if (con != null) {
				con.close();
			}
		}

		return e;
	}

	public void updateBlog(BlogEntry selectedBlogEntry) throws SQLException {
		Connection con = _ds.getConnection();
		try {
			boolean oldAutoCommitMode = con.getAutoCommit();
			con.setAutoCommit(false);

			PreparedStatement ps = con
					.prepareStatement("UPDATE blog_item SET ititle=?, ibody=? WHERE inumber=?");
			ps.setString(1, selectedBlogEntry.getTitle());
			ps.setString(2, selectedBlogEntry.getBody());
			ps.setLong(3, selectedBlogEntry.getId());
			ps.executeUpdate();
			ps.close();

			String sqlDelete = "DELETE FROM blog_item_categories WHERE item_id=?";
			String sqlInsert = "INSERT INTO blog_item_categories VALUES (?,?)";
			if (selectedBlogEntry.getCategories() != null
					&& selectedBlogEntry.getCategories().length > 0) {
				PreparedStatement psDelete = con.prepareStatement(sqlDelete);
				psDelete.setLong(1, selectedBlogEntry.getId());
				psDelete.executeUpdate();
				psDelete.close();

				PreparedStatement psInsert = con.prepareStatement(sqlInsert);
				// update categories
				for (Long catID : selectedBlogEntry.getCategories()) {
					psInsert.clearParameters();
					psInsert.setLong(1, selectedBlogEntry.getId());
					psInsert.setLong(2, catID);
					psInsert.addBatch();
				}
				psInsert.executeBatch();
				psInsert.close();
			}
			con.commit();
			con.setAutoCommit(oldAutoCommitMode);
		} catch (SQLException sqe) {
			con.rollback();
			throw sqe;
		} finally {
			con.close();
		}

	}

	public void createBlog(BlogEntry newBlog) throws SQLException {
		Connection con = _ds.getConnection();
		try {
			PreparedStatement ps = con
					.prepareStatement(
							"INSERT INTO blog_item (ititle, ibody, itime, inumber) VALUES (?,?,?,?)",
							Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, newBlog.getTitle());
			ps.setString(2, newBlog.getBody());
			ps.setTimestamp(3, new Timestamp(new Date().getTime()));
			ps.setObject(4, null);
			ps.executeUpdate();
			ResultSet generatedKeysRS = ps.getGeneratedKeys();
			if (generatedKeysRS.next()) {
				newBlog.setId(generatedKeysRS.getLong(1));
			} else {
				throw new SQLException(
						"Create Blog Entry failed. No new generated key");
			}
			generatedKeysRS.close();
			ps.close();

			String sqlInsert = "INSERT INTO blog_item_categories VALUES (?,?)";
			if (newBlog.getCategories() != null
					&& newBlog.getCategories().length > 0) {

				PreparedStatement psInsert = con.prepareStatement(sqlInsert);
				// update categories
				for (Long catID : newBlog.getCategories()) {
					psInsert.clearParameters();
					psInsert.setLong(1, newBlog.getId());
					psInsert.setLong(2, catID);
					psInsert.addBatch();
				}
				psInsert.executeBatch();
				psInsert.close();
			}
		} catch (SQLException sqe) {
			throw sqe;
		} finally {
			con.close();
		}
	}

}
