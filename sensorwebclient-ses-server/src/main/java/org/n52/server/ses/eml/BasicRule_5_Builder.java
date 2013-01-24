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
package org.n52.server.ses.eml;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.n52.client.view.gui.elements.layouts.SimpleRuleType;
import org.n52.server.ses.SesConfig;
import org.n52.server.ses.hibernate.HibernateUtil;
import org.n52.shared.serializable.pojos.BasicRule;
import org.n52.shared.serializable.pojos.Rule;
import org.n52.shared.serializable.pojos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BasicRule_5_Builder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRule_5_Builder.class);

    // TAG NAMES
    final static String propertyValue = "value";

    final static String simplePattern = "SimplePattern";

    final static String complexPattern = "ComplexPattern";

    final static String selectFunction = "SelectFunction";

    final static String patternReference = "PatternReference";

    final static String selectEvent = "SelectEvent";

    final static String userParameterValue = "UserParameterValue";

    final static String eventCount = "EventCount";

    final static String fesFilter = "fes:Filter";

    final static String valuereference = "fes:ValueReference";

    final static String fesLiteral = "fes:Literal";

    // ATTRIBUTE NAMES
    final static String patternID = "patternID";

    final static String newEventName = "newEventName";

    final static String eventName = "eventName";

    final static String outputName = "outputName";

    /**
     * Sensor Failure
     * 
     * This method builds the rule type "Sensor Failure" by loading and filling a template file.
     * The location of this file is defined in /properties/ses-client.properties 
     * in the variable "resLocation". File name must be BR_5.xml.
     * 
     * @param rule
     * @throws Exception
     * @return {@link BasicRule}
     */
    public static BasicRule create_BR_5(Rule rule) throws Exception {
        
    	// Get current user. This user is also the owner of the new rule
        User user = HibernateUtil.getUserByID(rule.getUserID());

        String eml;
        String finalEml;
        String title = rule.getTitle();

        // Pre-defined pattern IDs and event names. All names start with the title of the rule.
        // This is important to have unique names.
        ArrayList<String> simplePatternID = new ArrayList<String>();
        simplePatternID.add(title + "_incoming_observations_count_stream");

        ArrayList<String> simpleNewEventName = new ArrayList<String>();
        simpleNewEventName.add(title + "_incoming_observations_count");

        ArrayList<String> complexPatternID = new ArrayList<String>();
        complexPatternID.add(title + "_no_observations_received_stream");
        complexPatternID.add(title + "_observations_received_stream");
        complexPatternID.add(title + "_no_observation_notification_stream");
        complexPatternID.add(title + "_observation_notification_stream");

        ArrayList<String> complexNewEventName = new ArrayList<String>();
        complexNewEventName.add(title + "_no_observation_received");
        complexNewEventName.add(title + "_observation_received");
        complexNewEventName.add(title + "_no_observation_notification");
        complexNewEventName.add(title + "observation_notification");

        ArrayList<String> complexOutputname = new ArrayList<String>();
        complexOutputname.add(title + "_no_observation_output");
        complexOutputname.add(title + "_observation_output");

        // This ArrayList defines the references in the PatternReference tags. 
        // The references are ordered from first to the last pattern.
        ArrayList<String> patternReferenceText = new ArrayList<String>();
        patternReferenceText.add(simplePatternID.get(0));
        patternReferenceText.add(simplePatternID.get(0));
        patternReferenceText.add(simplePatternID.get(0));
        patternReferenceText.add(simplePatternID.get(0));
        
        patternReferenceText.add(complexPatternID.get(1));
        patternReferenceText.add(complexPatternID.get(0));
        patternReferenceText.add(complexPatternID.get(0));
        patternReferenceText.add(complexPatternID.get(1));

        // URL adress of the BR_5.xml file
        URL url = new URL(SesConfig.resLocation_5);

        // build document
        DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFac.newDocumentBuilder();
        Document doc = docBuilder.parse(url.openStream());

        // transformer for final output
        Transformer transormer = TransformerFactory.newInstance().newTransformer();
        transormer.setOutputProperty(OutputKeys.INDENT, "yes");

        // parse <SimplePattern>
        NodeList simplePatternList = doc.getElementsByTagName(simplePattern);
        for (int i = 0; i < simplePatternList.getLength(); i++) {
            Node fstNode = simplePatternList.item(i);
            Element fstElement = (Element) fstNode;

            // set patternID
            Node patternIdSimple = simplePatternList.item(i);
            patternIdSimple.getAttributes().getNamedItem(patternID).setTextContent(simplePatternID.get(i));

            // set newEventName of SelectFunction
            NodeList selectFunctionList = fstElement.getElementsByTagName(selectFunction);
            Node selectFunctionNode = selectFunctionList.item(0);
            selectFunctionNode.getAttributes().getNamedItem(newEventName)
            .setTextContent(simpleNewEventName.get(i));

            // set propertyRestrictions
            NodeList propertyRestrictiosnList = fstElement.getElementsByTagName(propertyValue);
            Node value_1 = propertyRestrictiosnList.item(0);
            value_1.setTextContent(rule.getPhenomenon());
            Node value_2 = propertyRestrictiosnList.item(1);
            value_2.setTextContent(rule.getProcedure());

            // set EventCount
            NodeList eventCountList = fstElement.getElementsByTagName(eventCount);
            if (eventCountList.getLength() != 0) {
                Node eventCountNode = eventCountList.item(0);
                eventCountNode.setTextContent(rule.getEntryCount());
            }

            // set UserParameterValue
            NodeList userParameterValueList = fstElement.getElementsByTagName(userParameterValue);
            Node userParameterValueNode = userParameterValueList.item(2);
            userParameterValueNode.setTextContent("PT" + rule.getEntryTime() + rule.getEntryTimeUnit());
        }

        // parse <ComplexPatterns>
        NodeList complexPatternList = doc.getElementsByTagName(complexPattern);
        for (int i = 0; i < complexPatternList.getLength(); i++) {
            Node fstNode = complexPatternList.item(i);
            Element fstElement = (Element) fstNode;

            // set patternID
            Node patternIdNode = complexPatternList.item(i);
            patternIdNode.getAttributes().getNamedItem(patternID).setTextContent(complexPatternID.get(i));

            // set newEventName of SelectFunction
            NodeList selectFunctionList = fstElement.getElementsByTagName(selectFunction);
            Node selectFunctionNode = selectFunctionList.item(0);
            selectFunctionNode.getAttributes().getNamedItem(newEventName).setTextContent(
                    complexNewEventName.get(i));
            if (selectFunctionNode.getAttributes().getNamedItem(outputName) != null) {
                selectFunctionNode.getAttributes().getNamedItem(outputName).setTextContent(
                        complexOutputname.get(i - 2));
            }

            // set PatternReference
            NodeList patterReferenceList = fstElement.getElementsByTagName(patternReference);
            for (int j = 0; j < patterReferenceList.getLength(); j++) {
                Node patterReferenceNode = patterReferenceList.item(j);
                if (j == 0) {
                    patterReferenceNode.setTextContent(patternReferenceText.get(2 * i));
                } else {
                    patterReferenceNode.setTextContent(patternReferenceText.get((2 * i) + 1));
                }
            }

            // set eventName of selectEvent
            NodeList selectEventList = fstElement.getElementsByTagName(selectEvent);
            if (selectEventList.getLength() != 0) {
                Node selectEventNode = selectEventList.item(0);
                selectEventNode.getAttributes().getNamedItem(eventName).setTextContent(simpleNewEventName.get(0));
            }

            // <fes:Filter>
            NodeList filterList = fstElement.getElementsByTagName(valuereference);
            if (filterList.getLength() != 0) {
                Node valueReferenceNode = filterList.item(0);
                valueReferenceNode.setTextContent(simpleNewEventName.get(0) + "/doubleValue");
            }
        }

        // final EML document. Convert document to string for saving in DB
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transormer.transform(source, result);

        eml = result.getWriter().toString();
        finalEml = eml;
        finalEml = finalEml.substring(finalEml.indexOf("<EML"));

        return new BasicRule(rule.getTitle(), "B", "BR5", rule.getDescription(), rule.isPublish(), user.getId(),
                finalEml, false);
    }

    /**
     * 
     * This method is used to parse an EML file and return a Rule class with rule specific attributes. 
     * The method is called if user want to edit this rule type.
     * 
     * @param eml
     * @return {@link Rule}
     */
    public static Rule getRuleByEML(String eml) {
        Rule rule = new Rule();

        try {
        	// build document
            DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFac.newDocumentBuilder();
            Document doc = docBuilder.parse(new ByteArrayInputStream(eml.getBytes()));

            // get phenomenon
            NodeList propertyRestrictiosnList = doc.getElementsByTagName(propertyValue);
            Node value_1 = propertyRestrictiosnList.item(0);
            rule.setPhenomenon(value_1.getTextContent());

            // get station
            Node value_2 = propertyRestrictiosnList.item(1);
            rule.setProcedure(value_2.getTextContent());

            NodeList userParameterValueList = doc.getElementsByTagName(userParameterValue);
            Node userParameterValueNode = userParameterValueList.item(2);
            String temp = userParameterValueNode.getTextContent();
            temp.substring(2);
            
            // rTime: time value
            rule.setrTime(temp.substring(2, temp.length()-1));
            
            // rTimeUnit: time unit
            rule.setrTimeUnit(temp.substring(temp.length()-1));
            
            // set rule type
            rule.setRuleType(SimpleRuleType.SENSOR_LOSS);

        } catch (Exception e) {
            LOGGER.error("Error parsing EML rule", e);
        }
        
        return rule;
    }
}