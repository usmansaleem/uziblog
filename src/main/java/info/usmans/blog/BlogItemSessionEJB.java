package info.usmans.blog;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;

/**
 * Session Bean implementation class BlogItemSessionFacade
 */
@Stateless
@LocalBean
public class BlogItemSessionEJB {
    private static final String BLOG_ITEM_BY_SECTION_COUNT_QUERY = "select count(inumber) from blog_item WHERE blog_section_name = ?";
    private static final String BLOG_ID_MODTIME_QUERY = "SELECT inumber, mtime FROM blog_item WHERE blog_section_name='Main' ORDER BY itime desc";
    private static final String BLOG_ITEM_BY_ID_QUERY = "SELECT inumber, ititle, ibody, imore, itime, mtime FROM blog_item WHERE inumber = ? order by itime desc ";
    private static final String UPDATE_BLOG_ITEM_QUERY = "UPDATE blog_item SET ititle=?, ibody=?, mtime=? WHERE inumber=?";
    private static final String BLOG_ITEM_CATEGORY_DELETE_QUERY = "DELETE FROM blog_item_categories WHERE item_id=?";
    private static final String BLOG_ITEM_CATEGORY_INSERT_QUERY = "INSERT INTO blog_item_categories VALUES (?,?)";
    private static final String BLOG_ITEM_INSERT_QUERY = "INSERT INTO blog_item (ititle, ibody, itime, mtime, blog_section_name) VALUES (?,?,?,?,?)";
    private static final String BLOG_SECTION_NAME_QUERY = "SELECT name FROM blog_section";
    private static final String BLOG_ITEM_BY_SECTION_QUERY = "SELECT inumber, ititle, ibody, imore, itime, mtime, blog_section_name FROM blog_item WHERE blog_section_name = ? ORDER BY itime DESC LIMIT ? OFFSET ?";
    private static final String CATEGORY_ID_QUERY = "SELECT c.id FROM blog_categories c, blog_item_categories ic WHERE c.id=ic.category_id AND ic.item_id=?";
    private static final String BLOG_CATEGORIES_QUERY = "select id, name from blog_categories";

    @Resource(mappedName = "java:/uziblogds")
    private DataSource _ds;

    /**
     * Default constructor.
     */
    public BlogItemSessionEJB() {
    }


    /**
     * Returns database product name and version.
     * This should only be called at application initialization time by the controller.
     */
    public String getDatabaseInfo() {
        String databaseInfo = "Unknown";
        try (Connection con = _ds.getConnection()) {
            java.sql.DatabaseMetaData metadata = con.getMetaData();
            databaseInfo = metadata.getDatabaseProductName() + " " + metadata.getDatabaseProductVersion();
        } catch (SQLException sqe) {
            sqe.printStackTrace();
        }
        return databaseInfo;
    }

    public int getCount(String section) throws SQLException {
        int count;
        try (Connection con = _ds.getConnection(); PreparedStatement ps = con
                .prepareStatement(BLOG_ITEM_BY_SECTION_COUNT_QUERY)) {
            ps.setString(1, section);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                count = rs.getInt(1);
            }


        }
        return count;
    }

    /**
     * Used by SiteMap servlet.
     * @return BlogEntry with id and modifiedTime populated only.
     * @throws SQLException
     */
    public List<BlogEntry> getMainBlogEntriesIdList() throws SQLException {
        String sql = BLOG_ID_MODTIME_QUERY;
        List<BlogEntry> ids = new LinkedList<>();
        try (Connection con = _ds.getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                BlogEntry entry = new BlogEntry();
                entry.setId(rs.getLong(1));
                entry.setModifiedOn(new Date(rs.getTimestamp(2).getTime()));
                ids.add(entry);
            }
        }
        return ids;
    }

    public List<BlogEntry> getBlogEntries(int offset, int limit, String section)
            throws SQLException {
        List<BlogEntry> blogEntryList = new LinkedList<>();
        try (Connection con = _ds.getConnection(); PreparedStatement ps = con
                .prepareStatement(BLOG_ITEM_BY_SECTION_QUERY); PreparedStatement ps2 = con
                .prepareStatement(CATEGORY_ID_QUERY)) {

            ps.setString(1, section);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BlogEntry e = new BlogEntry();
                    e.setId(rs.getInt(1));
                    e.setTitle(rs.getString(2));
                    e.setBody(rs.getString(3));
                    e.setMore(rs.getString(4));
                    e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
                    e.setModifiedOn(new Date(rs.getTimestamp(6).getTime()));
                    e.setBlogSection(rs.getString(7));
                    // load categories for this blog entry
                    List<Long> catSet = new LinkedList<>();
                    ps2.clearParameters();
                    ps2.setLong(1, e.getId());
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            catSet.add(rs2.getLong(1));
                        }
                    }
                    e.setCategories(catSet.toArray(new Long[catSet.size()]));
                    blogEntryList.add(e);
                }
            }

        }
        return blogEntryList;
    }

    /**
     * Returns top 10 entries for global RSS feed
     *
     * @return List of BlogEntry
     * @throws SQLException
     */
    public List<BlogEntry> getTopTenBlogEntries() throws SQLException {
        return getBlogEntries(0, 10, "Main");
    }

    /**
     * Return all categories for blog. This should be called once at application
     * initialization time as the categories are not usually change. This is
     * designed to be used by selectItems
     *
     * @return Map of categories.
     */
    public Map<String, Long> getCategoriesMap() throws SQLException {
        Map<String, Long> map = new TreeMap<>();
        try (Connection con = _ds.getConnection(); PreparedStatement ps = con
                .prepareStatement(BLOG_CATEGORIES_QUERY); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString(2), rs.getLong(1));
            }
        }
        return map;
    }

    /**
     * Return blog entries ordered by category and section
     */
    public List<BlogEntry> getBlogEntriesByCategory(long catID, String section)
            throws SQLException {
        Connection con = null;
        List<BlogEntry> blogEntry = new LinkedList<>();
        try {
            con = _ds.getConnection();
            PreparedStatement ps2 = con
                    .prepareStatement(CATEGORY_ID_QUERY);

            PreparedStatement ps = con
                    .prepareStatement("SELECT inumber, ititle, ibody, imore, itime, blog_section_name FROM blog_item INNER JOIN blog_item_categories ON inumber = item_id WHERE category_id = ? AND blog_section_name = ? order by itime desc ");
            ps.setLong(1, catID);
            ps.setString(2, section);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BlogEntry e = new BlogEntry();
                e.setId(rs.getInt(1));
                e.setTitle(rs.getString(2));
                e.setBody(rs.getString(3));
                e.setMore(rs.getString(4));
                e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
                e.setBlogSection(rs.getString(6));
                // load categories for this blog entry
                List<Long> catSet = new LinkedList<>();
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
     * @param catID Category ID
     * @return List of Blog Entries
     * @throws SQLException
     */
    public List<BlogEntry> getTopTenBlogEntriesByCategory(long catID)
            throws SQLException {
        Connection con = null;
        List<BlogEntry> blogEntry = new LinkedList<>();
        try {
            con = _ds.getConnection();
            PreparedStatement ps2 = con
                    .prepareStatement(CATEGORY_ID_QUERY);

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
                List<Long> catSet = new LinkedList<>();
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
        e.setModifiedOn(new Date());

        try (Connection con = _ds.getConnection(); PreparedStatement ps2 = con.prepareStatement(CATEGORY_ID_QUERY);
             PreparedStatement ps = con.prepareStatement(BLOG_ITEM_BY_ID_QUERY)) {

            ps.setLong(1, blogID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    e.setId(rs.getInt(1));
                    e.setTitle(rs.getString(2));
                    e.setBody(rs.getString(3));
                    e.setMore(rs.getString(4));
                    e.setCreatedon(new Date(rs.getTimestamp(5).getTime()));
                    e.setModifiedOn(new Date(rs.getTimestamp(6).getTime()));
                    // load categories for this blog entry
                    List<Long> catSet = new LinkedList<>();
                    ps2.clearParameters();
                    ps2.setLong(1, e.getId());
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            catSet.add(rs2.getLong(1));
                        }
                    }
                    e.setCategories(catSet.toArray(new Long[catSet.size()]));
                }
            }
        }
        return e;
    }

    /**
     * Update blog entry. Deletes old categories and recreate them again if required.
     *
     * @param selectedBlogEntry An instance of BlogEntry. Expecting title, body, id (and optionally categories) to be
     *                          populated.
     * @throws SQLException
     */
    public void updateBlog(BlogEntry selectedBlogEntry) throws SQLException {
        try (Connection con = _ds.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_BLOG_ITEM_QUERY);
             PreparedStatement psDelete = con.prepareStatement(BLOG_ITEM_CATEGORY_DELETE_QUERY);
             PreparedStatement psInsert = con.prepareStatement(BLOG_ITEM_CATEGORY_INSERT_QUERY)
        ) {


                ps.setString(1, selectedBlogEntry.getTitle());
                ps.setString(2, selectedBlogEntry.getBody());
                ps.setTimestamp(3, new Timestamp(new Date().getTime()));
                ps.setLong(4, selectedBlogEntry.getId());
                ps.executeUpdate();

                if (selectedBlogEntry.getCategories() != null
                        && selectedBlogEntry.getCategories().length > 0) {
                    //first delete existing categories
                    psDelete.setLong(1, selectedBlogEntry.getId());
                    psDelete.executeUpdate();

                    // now recreate them
                    for (Long catID : selectedBlogEntry.getCategories()) {
                        psInsert.clearParameters();
                        psInsert.setLong(1, selectedBlogEntry.getId());
                        psInsert.setLong(2, catID);
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch();
                }
        }

    }

    public void createBlog(BlogEntry newBlog) throws SQLException {
        try (Connection con = _ds.getConnection(); PreparedStatement ps = con.prepareStatement(BLOG_ITEM_INSERT_QUERY,
                Statement.RETURN_GENERATED_KEYS); PreparedStatement psInsert = con.prepareStatement(BLOG_ITEM_CATEGORY_INSERT_QUERY)) {

            ps.setString(1, newBlog.getTitle());
            ps.setString(2, newBlog.getBody());
            ps.setTimestamp(3, new Timestamp(new Date().getTime()));
            ps.setTimestamp(4, new Timestamp(new Date().getTime()));
            ps.setString(5, newBlog.getBlogSection());
            ps.executeUpdate();
            try (ResultSet generatedKeysRS = ps.getGeneratedKeys()) {
                if (generatedKeysRS.next()) {
                    newBlog.setId(generatedKeysRS.getLong(1));
                } else {
                    throw new RuntimeException(
                            "Create Blog Entry failed. No new generated key");
                }
            }

            //create categories
            if (newBlog.getCategories() != null
                    && newBlog.getCategories().length > 0) {

                for (Long catID : newBlog.getCategories()) {
                    psInsert.clearParameters();
                    psInsert.setLong(1, newBlog.getId());
                    psInsert.setLong(2, catID);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }
        }
    }

    /**
     * Return Blog Sections list.
     *
     * @return List of section names
     */
    public List<String> getBlogSectionsList() throws SQLException {
        List<String> list = new LinkedList<>();
        try (Connection con = _ds.getConnection();
             PreparedStatement ps = con.prepareStatement(BLOG_SECTION_NAME_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        }
        return list;
    }

}
