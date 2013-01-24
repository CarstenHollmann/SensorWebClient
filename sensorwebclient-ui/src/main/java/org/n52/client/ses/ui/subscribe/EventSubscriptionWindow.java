/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.client.ses.ui.subscribe;

import static com.google.gwt.user.client.Cookies.getCookie;
import static com.smartgwt.client.types.Alignment.RIGHT;
import static org.n52.client.ses.ctrl.SesRequestManager.COOKIE_USER_ID;
import static org.n52.client.ses.i18n.SesStringsAccessor.i18n;
import static org.n52.client.view.gui.elements.layouts.SimpleRuleType.OVER_UNDERSHOOT;

import org.n52.client.bus.EventBus;
import org.n52.client.ses.event.CreateSimpleRuleEvent;
import org.n52.client.ses.event.SubscribeEvent;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.ui.ApplyCancelButtonLayout;
import org.n52.client.view.gui.elements.layouts.SimpleRuleType;
import org.n52.shared.serializable.pojos.Rule;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

public class EventSubscriptionWindow extends Window {

    private static final String COMPONENT_ID = "eventSubscriptionWindow";

    private static int WIDTH = 950;

    private static int HEIGHT = 550;

    private static EventSubscriptionController controller;

    private static EventSubscriptionWindow instance;

    private Layout ruleTemplateEditCanvas;

    private Layout content;

    public static EventSubscriptionWindow getInst(TimeSeries dataItem) {
        if (instance == null) {
            controller = new EventSubscriptionController();
            instance = new EventSubscriptionWindow(controller);
        }
        controller.setTimeseries(dataItem);
        return instance;
    }

    public EventSubscriptionWindow(EventSubscriptionController controller) {
        setStyleName("n52_sensorweb_client_event_subscription_window");
        controller.setEventSubscription(this);
        initializeWindow();
        addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClickEvent event) {
                hide();
            }
        });
    }

    private void initializeWindow() {
        setID(COMPONENT_ID);
        setShowModalMask(true);
        setIsModal(true);
        setCanDragResize(true);
        setShowMaximizeButton(true);
        setShowMinimizeButton(false);
        setMargin(10);
        setTitle(i18n.createAboWindowTitle());
        setWidth(WIDTH);
        setHeight(HEIGHT);
        centerInPage();
        addResizedHandler(new ResizedHandler() {
            @Override
            public void onResized(ResizedEvent event) {
                WIDTH = EventSubscriptionWindow.this.getWidth();
                HEIGHT = EventSubscriptionWindow.this.getHeight();
            }
        });
    }
    
    @Override
    public void show() {
        super.show();
        if (content != null) {
            removeItem(content);
        }
        initializeContent();
        redraw();
    }

    private void initializeContent() {
        content = new HLayout();
        content.setStyleName("n52_sensorweb_client_create_abo_window_content");
        content.addMember(createNewEventAbonnementCanvas());
        content.addMember(createContextWindowHelp());
        addItem(content);
    }

    private Canvas createNewEventAbonnementCanvas() {
        Layout subscriptionContent = new VLayout();
        subscriptionContent.setStyleName("n52_sensorweb_client_create_abo_form_content");
        subscriptionContent.addMember(createStationInfo());
        subscriptionContent.addMember(createRuleTemplateSelectionCanvas());
        subscriptionContent.addMember(new EventNameForm(controller));
        subscriptionContent.addMember(createApplyCancelCanvas());
        subscriptionContent.addMember(new TimeSeriesMetadataTable(controller));
        return subscriptionContent;
    }

    private Canvas createStationInfo() {
        TimeSeries timeSeries = controller.getTimeSeries();
        StaticTextItem stationName = new StaticTextItem();
        stationName.setTitle(i18n.station());
        stationName.setValue(timeSeries.getStationName());
        StaticTextItem parameter = new StaticTextItem();
        parameter.setTitle(i18n.phenomenon());
        parameter.setValue(timeSeries.getPhenomenonId());
        DynamicForm form = new DynamicForm();
        form.setFields(stationName, parameter);
        return form;
    }

    private Canvas createRuleTemplateSelectionCanvas() {
        Layout selectionCanvas = new VLayout();
        SelectSubscriptionForm selectionRadioForm = new SelectSubscriptionForm(controller);
        RuleTemplate template = selectionRadioForm.getDefaultRuleTemplate();
        Canvas ruleTemplateEditCanvas = createRuleTemplateEditorCanvas(template);
        selectionCanvas.addMember(selectionRadioForm);
        selectionCanvas.addMember(ruleTemplateEditCanvas);
        return selectionCanvas;
    }

    private Canvas createRuleTemplateEditorCanvas(RuleTemplate template) {
        if (ruleTemplateEditCanvas == null) {
            ruleTemplateEditCanvas = new VLayout();
            ruleTemplateEditCanvas.setStyleName("n52_sensorweb_client_edit_create_abo_edit");
            ruleTemplateEditCanvas.addMember(template.createEditCanvas());
        }
        return ruleTemplateEditCanvas;
    }

    private Canvas createApplyCancelCanvas() {
        ApplyCancelButtonLayout applyCancel = new ApplyCancelButtonLayout();
        applyCancel.setStyleName("n52_sensorweb_client_create_abo_applycancel");
        applyCancel.createApplyButton(i18n.create(), i18n.create(), createApplyHandler());
        applyCancel.createCancelButton(i18n.cancel(), i18n.cancel(), createCancelHandler());
        applyCancel.setAlign(RIGHT);
        return applyCancel;
    }

    private ClickHandler createApplyHandler() {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Rule rule = controller.createSimpleRuleFromSelection();
                CreateSimpleRuleEvent createEvt = new CreateSimpleRuleEvent(rule, false, "");
                EventBus.getMainEventBus().fireEvent(createEvt);
                
                // TODO we directly want to subscribe => make asynchronous event
                String ruleName = rule.getTitle();
                String cookie = getCookie(COOKIE_USER_ID);
                SubscribeEvent subscribeEvt = new SubscribeEvent(cookie, ruleName, "email", "text");
                EventBus.getMainEventBus().fireEvent(subscribeEvt);
            }
        };
    }

    private ClickHandler createCancelHandler() {
        return new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                EventSubscriptionWindow.this.hide();
            }
        };
    }

    private Canvas createContextWindowHelp() {
        Layout contextHelpContent = new VLayout();
        contextHelpContent.setStyleName("n52_sensorweb_client_create_abo_context_help");
            
            // TODO Auto-generated method stub
        
        return contextHelpContent;
    }


    public String getId() {
        return COMPONENT_ID;
    }

    public void setTimeseries(TimeSeries timeseries) {
        controller.setTimeseries(timeseries);
    }

    public void updateRuleEditCanvas(RuleTemplate template) {
        for (Canvas canvas : ruleTemplateEditCanvas.getMembers()) {
            canvas.removeFromParent();
            canvas.destroy();
        }
        Canvas ruleEditCanvas = template.createEditCanvas();
        ruleTemplateEditCanvas.addMember(ruleEditCanvas);
        ruleTemplateEditCanvas.redraw();
    }

}