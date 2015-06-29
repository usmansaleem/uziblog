package info.usmans.blog.data;

import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.ScheduleEvent;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * EJB to manage salat scheduler
 * @author Usman Saleem
 */
@Stateless
@LocalBean
public class SalatSchedulerEJB {
    @Resource(mappedName = "java:/uziblogds")
    private DataSource _ds;

    /**
     * @param start
     * @param end
     * @return
     */
    public List<ScheduleEvent> loadEvents(Date start, Date end) throws SQLException {
       List<ScheduleEvent> eventsList = new LinkedList<>();
        try(Connection con = _ds.getConnection(); PreparedStatement ps = con
                .prepareStatement("SELECT salat_name, date_offered FROM salattracker WHERE date_offered >= ? AND date_offered <= ?")) {
            ps.setDate(1, new java.sql.Date(start.getTime()));
            ps.setDate(2, new java.sql.Date(end.getTime()));
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    eventsList.add(new DefaultScheduleEvent(rs.getString(1), rs.getDate(2), rs.getDate(2)));
                }
            }
        }

        return eventsList;
    }

    /**
     *
     * @param title
     * @param date
     * @throws SQLException
     */
    public void addEvent(String title, Date date) throws SQLException {
        try(Connection con = _ds.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO SALATTRACKER VALUES(?,?)")) {
            ps.setString(1, title);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.executeUpdate();
        }

    }
}
