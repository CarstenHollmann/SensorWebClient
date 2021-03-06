/**
 * Copyright (C) 2012-2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.client.ui;

import java.util.ArrayList;
import java.util.Date;

import org.n52.shared.Constants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.AnimationCallback;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class Toaster {

    /**
     * how long the toaster message appears to the user.
     */
    private Integer fadeout = new Integer(2000); // default

    private int width;

    private int height;

    private String title;

    private int left;

    private int top;

    private Canvas parentElem;

    private Window toasterWindow;

    private String id;

    protected Timer animationTimer;

    private boolean isVisible = false;

    private static Toaster instance;

    private VLayout layout;

    private ArrayList<Label> messages;

    private Toaster(String id, int width, int height, String title, Canvas parentElem, int fadeout) {
        this.id = id;
        this.height = height;
        this.width = width;
        this.title = title;
        this.parentElem = parentElem;
        this.fadeout = fadeout;
        this.messages = new ArrayList<Label>();
        this.animationTimer = new Timer() {
            @Override
            public void run() {
                hide();
            }
        };
    }
    
    public static Toaster getToasterInstance() {
        if (instance == null) {
            throw new IllegalStateException("Create first a Toaster instance with createInstance(...).");
        }
        return instance;
    }

    public static Toaster createToasterInstance(String id, int width, int height, String title, Canvas parentElem, int fadeout) {

        if (instance == null) {
            instance = new Toaster(id, width, height, title, parentElem, fadeout);
            instance.getToasterWindow();
        } else {
            GWT.log("Will not create another Toaster instance. Return already existing one.");
        }
        return instance;
    }

    private void getToasterWindow() {

        if (this.toasterWindow == null) {
            this.toasterWindow = new Window();
            this.layout = new VLayout();
            this.layout.setTabIndex( -1);

            this.toasterWindow.setParentElement(this.parentElem);
            this.toasterWindow.setAnimateFadeTime(fadeout);

            this.toasterWindow.setHeight(this.height);
            this.toasterWindow.setWidth(this.width);
            this.toasterWindow.setTitle(this.title);
            this.toasterWindow.setAutoSize(new Boolean(false));
            this.toasterWindow.setOverflow(Overflow.AUTO);
            this.left = this.toasterWindow.getParentElement().getWidth().intValue() - this.width - 10;
            this.top = this.toasterWindow.getParentElement().getHeight().intValue() - this.height - 30;

            this.toasterWindow.setLeft(this.left);
            this.toasterWindow.setTop(this.top);
            this.toasterWindow.setCanDragResize(true);
            this.toasterWindow.setShowMaximizeButton(true);

            this.toasterWindow.setID(this.id);
            this.toasterWindow.addItem(this.layout);

            this.toasterWindow.addCloseClickHandler(new CloseClickHandler() {

                public void onCloseClick(CloseClickEvent event) {
                    hide();
                }
            });
        }
    }
 
    public boolean isShown() {
        return this.isVisible;
    }


    public void addMessage(String msg) {

        Date d = new Date();
        String timeStamp = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm").format(d);

        Label l = new Label(timeStamp + " " + msg);
        l.setCanSelectText(true);
        l.setStyleName("n52_sensorweb_client_toasterMsg");
        l.setAutoHeight();

        this.messages.add(l);

        for (int i = 0; i < this.messages.size(); i++) {
            if (this.layout.hasMember(this.messages.get(i))) {
                this.layout.removeMember(this.messages.get(i));
            }
        }
        for (int i = this.messages.size() - 1; i >= 0; i--) {
            this.layout.addMember(this.messages.get(i));
        }

//        this.left = this.toasterWindow.getParentElement().getWidth().intValue() - this.width - 10;
//        this.top = this.toasterWindow.getParentElement().getHeight().intValue() - this.height - 30;
//
//        this.toasterWindow.setLeft(this.left);
//        this.toasterWindow.setTop(this.top);

        animateToaster();
    }

    public void addErrorMessage(String error) {

        Date d = new Date();
        String timeStamp = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm").format(d);

        Label l = new Label(timeStamp + " " + error);
        l.setCanSelectText(true);
        l.setStyleName("n52_sensorweb_client_toasterErrorMsg");
        l.setAutoHeight();

        for (int i = 0; i < this.messages.size(); i++) {
            this.layout.removeMember(this.messages.get(i));

        }
        this.messages.add(l);
        for (int i = this.messages.size() - 1; i >= 0; i--) {
            this.layout.addMember(this.messages.get(i));
        }

        this.left = this.toasterWindow.getParentElement().getWidth().intValue() - this.width - 10;
        this.top = this.toasterWindow.getParentElement().getHeight().intValue() - this.height - 30;

        this.toasterWindow.setLeft(this.left);
        this.toasterWindow.setTop(this.top);

        animateToaster();
    }

    public void show() {
        if (this.toasterWindow == null) {
            getToasterWindow();
        }

        this.toasterWindow.setZIndex(Constants.Z_INDEX_ON_TOP);

        this.left = this.toasterWindow.getParentElement().getWidth().intValue() - this.width - 10;
        this.top = this.toasterWindow.getParentElement().getHeight().intValue() - this.height - 30;

        this.toasterWindow.setLeft(this.left);
        this.toasterWindow.setTop(this.top);

        this.toasterWindow.animateFade(100);
        this.isVisible = true;
    }

    public void hide() {
        this.toasterWindow.animateFade(0, new AnimationCallback() {
            public void execute(boolean earlyFinish) {
                Toaster.this.toasterWindow.setZIndex(0);
                Toaster.this.toasterWindow.hide();
            }
        });
        this.isVisible = false;
    }

    private void animateToaster() {
        this.isVisible = true;
        this.toasterWindow.setZIndex(Constants.Z_INDEX_ON_TOP);
        this.toasterWindow.animateFade(100, new AnimationCallback() {
            public void execute(boolean earlyFinish) {
                Toaster.this.animationTimer.schedule(2000);
            }
        });
    }

}