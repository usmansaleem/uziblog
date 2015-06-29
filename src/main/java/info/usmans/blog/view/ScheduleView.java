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
    private DefaultScheduleEvent event = new DefaultScheduleEvent();

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

    public DefaultScheduleEvent getEvent() {
        return event;
    }

    public void setEvent(DefaultScheduleEvent event) {
        this.event = event;
    }

    /**
     * Called by the dialog which gets visible after clicking on the date from the JSF page
     * @param actionEvent
     */
    public void addEvent(ActionEvent actionEvent) {
        if (event.getId() == null) {
            try {
                salatSchedulerEJB.addEvent(event.getTitle(), event.getStartDate());
            } catch (SQLException e) {
               e.printStackTrace();
            }
        }
        event = new DefaultScheduleEvent();
    }

    /**
     * A new event gets created when user clicks on a day in the scheduler. It will be saved in database via addEvent,
     * which gets called from the dialog.
     * @param selectEvent
     */
    public void onDateSelect(SelectEvent selectEvent) {
        event = new DefaultScheduleEvent("Fajar", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
    }
}