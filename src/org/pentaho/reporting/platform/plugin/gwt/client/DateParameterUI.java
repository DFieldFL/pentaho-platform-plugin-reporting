package org.pentaho.reporting.platform.plugin.gwt.client;

import java.util.Date;
import java.util.List;

import org.pentaho.gwt.widgets.client.datepicker.PentahoDatePicker;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;
import com.google.gwt.xml.client.Element;

public class DateParameterUI extends SimplePanel
{

  private class DateParameterSelectionHandler implements ValueChangeHandler<Date>
  {
    private List<String> parameterSelections;
    private ParameterControllerPanel controller;

    public DateParameterSelectionHandler(final List<String> parameterSelections, final ParameterControllerPanel controller)
    {
      this.parameterSelections = parameterSelections;
      this.controller = controller;
    }

    public void onValueChange(ValueChangeEvent<Date> event)
    {
      parameterSelections.clear();
      Date newDate = event.getValue();
      // add date as long
      parameterSelections.add("" + newDate.getTime());
      controller.fetchParameters(true);
    }

  }

  public DateParameterUI(final ParameterControllerPanel controller, final List<String> parameterSelections, final Element parameterElement)
  {
    // selectionsList should only have 1 date
    Date date = new Date();
    if (parameterSelections.size() > 0)
    {
      date = new Date(Long.parseLong(parameterSelections.get(0)));
    }
    else
    {
      // add the current date as the default, otherwise, a submission of another parameter
      // will not result in this parameter being submitted
      parameterSelections.clear();
      parameterSelections.add("" + date.getTime());
    }

    DefaultFormat format = new DefaultFormat(DateTimeFormat.getLongDateFormat());
    DateBox datePicker = new DateBox(new PentahoDatePicker(), date, format);

    datePicker.addValueChangeHandler(new DateParameterSelectionHandler(parameterSelections, controller));
    setWidget(datePicker);
  }

}