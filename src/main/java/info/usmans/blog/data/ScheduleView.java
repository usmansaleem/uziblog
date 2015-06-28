package info.usmans.blog.data;

/**
 * ScheduleView provides data for our salat tracker
 *
 * @author Usman Saleem
 */

import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Salat Scheduler Bean using PrimeFaces Scheuler component.
 *
 * Database table: salattracker(salat_name varchar(10) not null, date_offered date not null)
 */
@ManagedBean
@ViewScoped
public class ScheduleView implements Serializable {

    private ScheduleModel eventModel;
    private ScheduleEvent event = new DefaultScheduleEvent();

    /**
     * Initialize from database based on the date range.
     */
    @PostConstruct
    public void init() {
        eventModel = new LazyScheduleModel() {

            @Override
            public void loadEvents(Date start, Date end) {
                //TODO: load them from database based on passed date range

            }
        };
    }

    public ScheduleModel getEventModel() {
        return eventModel;
    }

    public ScheduleEvent getEvent() {
        return event;
    }

    public void setEvent(ScheduleEvent event) {
        this.event = event;
    }

    /**
     * Called by the dialog which gets visible after clicking on the date from the JSF page
     * @param actionEvent
     */
    public void addEvent(ActionEvent actionEvent) {
        if (event.getId() == null) {
            eventModel.addEvent(event); //TODO: update in database as well
        }
        else {
            eventModel.updateEvent(event);
        }

        event = new DefaultScheduleEvent(); //set a clean event after 'Reset' from dialog
    }

    /**
     * A new event gets created when user clicks on a day in the scheduler. It will be saved in database via addEvent,
     * which gets called from the dialog.
     * @param selectEvent
     */
    public void onDateSelect(SelectEvent selectEvent) {
        event = new DefaultScheduleEvent("", (Date) selectEvent.getObject(), (Date) selectEvent.getObject());
    }

}