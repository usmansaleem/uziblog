package info.usmans.blog.view;

/**
 * ScheduleView provides data for our salat tracker
 *
 * @author Usman Saleem
 */

import info.usmans.blog.data.SalatSchedulerEJB;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.*;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Salat Scheduler Bean using PrimeFaces Scheuler component.
 */
@ManagedBean
@ViewScoped
public class ScheduleView implements Serializable {

    @Inject
    private SalatSchedulerEJB salatSchedulerEJB;
    private ScheduleModel eventModel;
    private String[] salatEvents = new String[0];
    private Date selectedDate;

    /**
     * Initialize from database based on the date range.
     */
    @PostConstruct
    public void init() {
        eventModel = new LazyScheduleModel() {
            @Override
            public void loadEvents(Date start, Date end) {
                try {
                    List<ScheduleEvent> scheduleEvents = salatSchedulerEJB.loadEvents(start, end);
                    for (ScheduleEvent scheduleEvent : scheduleEvents) {
                        addEvent(scheduleEvent);
                    }
                }catch (SQLException sqe) {
                    sqe.printStackTrace();
                }
            }};
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public String[] getSalatEvents() {
        return salatEvents;
    }

    public void setSalatEvents(String[] salatEvents) {
        this.salatEvents = salatEvents;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    /**
     * Called by the dialog which gets visible after clicking on the date from the JSF page
     * @param actionEvent
     */
    public void addEvent(ActionEvent actionEvent) {
        for (String salatEvent : salatEvents) {
            try {
                salatSchedulerEJB.addEvent(salatEvent, selectedDate);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A new event gets created when user clicks on a day in the scheduler. It will be saved in database via addEvent,
     * which gets called from the dialog.
     * @param selectEvent
     */
    public void onDateSelect(SelectEvent selectEvent) {
        this.selectedDate = (Date) selectEvent.getObject();
    }
}