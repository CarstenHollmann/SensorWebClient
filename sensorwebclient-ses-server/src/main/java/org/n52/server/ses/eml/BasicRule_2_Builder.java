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
package org.n52.server.ses.eml;

import static org.n52.shared.util.MathSymbolUtil.getFesFilterFor;
import static org.n52.shared.util.MathSymbolUtil.getSymbolIndexForFilter;

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
import org.n52.server.ses.util.SESUnitConverter;
import org.n52.shared.serializable.pojos.BasicRule;
import org.n52.shared.serializable.pojos.Rule;
import org.n52.shared.serializable.pojos.TimeseriesMetadata;
import org.n52.shared.serializable.pojos.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Deprecated
public class BasicRule_2_Builder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRule_2_Builder.class);

    // TAG NAMES
    final static String propertyValue = "value";

    final static String simplePattern = "SimplePattern";

    final static String complexPattern = "ComplexPattern";

    final static String selectFunction = "SelectFunction";

    final static String patternReference = "PatternReference";

    final static String selectEvent = "SelectEvent";

    final static String userParameterValue = "UserParameterValue";

    final static String fesFilter = "fes:Filter";

    final static String valuereference = "fes:ValueReference";

    final static String fesLiteral = "fes:Literal";

    final static String duration = "Duration";

    // ATTRIBUTE NAMES
    final static String patternID = "patternID";

    final static String newEventName = "newEventName";

    final static String eventName = "eventName";

    final static String outputName = "outputName";

    /**
     * Trend over Time
     * 
     * This method builds the rule type "Trend over Time" by loading and filling a template file.
     * The location of this file is defined in /properties/ses-client.properties 
     * in the variable "resLocation". File name must be BR_2.xml.
     * 
     * @param rule
     * @return {@link BasicRule}
     * @throws Exception
     */
    public static BasicRule create_BR_2(Rule rule) throws Exception {
        
    	// Get current user. This user is also the owner of the new rule
        User user = HibernateUtil.getUserBy(rule.getUserID());

        String eml;
        String finalEml;
        String title = rule.getTitle();

        // Pre-defined pattern IDs and event names. All names start with the title of the rule.
        // This is important to have unique names.
        ArrayList<String> simplePatternID = new ArrayList<String>();
        simplePatternID.add(title + "_first_event_stream");
        simplePatternID.add(title + "_last_event_stream");

        ArrayList<String> simpleNewEventName = new ArrayList<String>();
        simpleNewEventName.add(title + "_first_event");
        simpleNewEventName.add(title + "_last_event");

        ArrayList<String> complexPatternID = new ArrayList<String>();
        complexPatternID.add(title + "_simple_trend_stream");
        complexPatternID.add(title + "_trend_overshoot_stream");
        complexPatternID.add(title + "_trend_undershoot_stream");
        complexPatternID.add(title + "_overshoot_notification_stream");
        complexPatternID.add(title + "_undershoot_notification_stream");

        ArrayList<String> complexNewEventName = new ArrayList<String>();
        complexNewEventName.add(title + "_simple_trend");
        complexNewEventName.add(title + "_trend_overshoot");
        complexNewEventName.add(title + "_trend_undershoot");
        complexNewEventName.add(title + "_overshoot_notification");
        complexNewEventName.add(title + "_undershoot_notification");

        ArrayList<String> complexOutputname = new ArrayList<String>();
        complexOutputname.add(title + "_overshoot_output");
        complexOutputname.add(title + "_undershoot_output");

        // This ArrayList defines the references in the PatternReference tags. 
        // The references are ordered from first to the last pattern.
        ArrayList<String> patternReferenceText = new ArrayList<String>();
        patternReferenceText.add(simplePatternID.get(0));
        patternReferenceText.add(simplePatternID.get(1));
        patternReferenceText.add(complexPatternID.get(0));
        patternReferenceText.add(complexPatternID.get(0));
        patternReferenceText.add(complexPatternID.get(0));
        patternReferenceText.add(complexPatternID.get(0));
        patternReferenceText.add(complexPatternID.get(2));
        patternReferenceText.add(complexPatternID.get(1));
        patternReferenceText.add(complexPatternID.get(1));
        patternReferenceText.add(complexPatternID.get(2));

        // build document
        URL url = new URL(SesConfig.resLocation_2);

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

            // set patternIDs
            Node patternIdSimple = simplePatternList.item(i);
            patternIdSimple.getAttributes().getNamedItem(patternID).setTextContent(simplePatternID.get(i));

            // set newEventName of SelectFunction
            NodeList selectFunctionList = fstElement.getElementsByTagName(selectFunction);
            Node selectFunctionNode = selectFunctionList.item(0);
            selectFunctionNode.getAttributes().getNamedItem(newEventName)
                    .setTextContent(simpleNewEventName.get(i));

            // set <Duration>
            NodeList durationList = fstElement.getElementsByTagName(duration);
            if (durationList.getLength() != 0) {
                Node durationNode = durationList.item(0);
                durationNode.setTextContent("PT" + rule.getEntryTime() + rule.getEntryTimeUnit());
            }

            // set propertyRestrictions
            TimeseriesMetadata metadata = rule.getTimeseriesMetadata();
            NodeList propertyRestrictiosnList = fstElement.getElementsByTagName(propertyValue);
            Node value_1 = propertyRestrictiosnList.item(0);
            value_1.setTextContent(metadata.getPhenomenon());
            Node value_2 = propertyRestrictiosnList.item(1);
            value_2.setTextContent(metadata.getGlobalSesId());

            // set UserParameterValue
            if (i == 1) {
                NodeList userParameterValueList = fstElement.getElementsByTagName(userParameterValue);
                Node userParameterValueNode = userParameterValueList.item(2);
                userParameterValueNode.setTextContent("PT" + rule.getEntryTime() + rule.getEntryTimeUnit());
            }

        }

        // parse <ComplexPatterns>
        NodeList complexPatternList = doc.getElementsByTagName(complexPattern);
        for (int i = 0; i < complexPatternList.getLength(); i++) {
            Node fstNode = complexPatternList.item(i);
            Element fstElement = (Element) fstNode;

            // set patternIDs
            Node patternIdNode = complexPatternList.item(i);
            patternIdNode.getAttributes().getNamedItem(patternID).setTextContent(complexPatternID.get(i));

            // set newEventName of SelectFunction
            NodeList selectFunctionList = fstElement.getElementsByTagName(selectFunction);
            Node selectFunctionNode = selectFunctionList.item(0);
            selectFunctionNode.getAttributes().getNamedItem(newEventName).setTextContent(
                    complexNewEventName.get(i));
            if (selectFunctionNode.getAttributes().getNamedItem(outputName) != null) {
                selectFunctionNode.getAttributes().getNamedItem(outputName).setTextContent(
                        complexOutputname.get(i - 3));
            }

            // set UserParameterValue of UserDefinedSelectFunction
            NodeList userParameterValueList = fstElement.getElementsByTagName(userParameterValue);
            if (userParameterValueList.getLength() != 0) {
                Node userParameterValueNode_1 = userParameterValueList.item(0);
                userParameterValueNode_1.setTextContent(simpleNewEventName.get(1) + "/doubleValue");
                Node userParameterValueNode_2 = userParameterValueList.item(1);
                userParameterValueNode_2.setTextContent(simpleNewEventName.get(0) + "/doubleValue");
            }

            // set PatternReference in the right order
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
                selectEventNode.getAttributes().getNamedItem(eventName).setTextContent(complexNewEventName.get(0));
            }

            // set <fes:Filter>
            NodeList filterList = doc.getElementsByTagName(fesFilter);
            for (int j = 0; j < filterList.getLength(); j++) {

                Node n = filterList.item(j);


                Node entryFilter = doc.createElement(getFesFilterFor(rule.getEntryOperatorIndex()));
                Node exitFilter = doc.createElement(getFesFilterFor(rule.getExitOperatorIndex()));

                Node valueReferenceNode = doc.createElement(valuereference);
                valueReferenceNode.setTextContent(complexNewEventName.get(0) + "/doubleValue");

                // Unit Conversion
                SESUnitConverter converter = new SESUnitConverter();
//                Object[] resultrUnit = converter.convert(rule.getEntryUnit(), Double.valueOf(rule.getEntryValue()));
//                Object[] resultcUnit = converter.convert(rule.getExitUnit(), Double.valueOf(rule.getExitValue()));

                Node fesLiteralNode = doc.createElement(fesLiteral);

                // add first filter to document
                if ((j == 0) && (i == 0)) {
//                    fesLiteralNode.setTextContent(resultrUnit[1].toString());
                    fesLiteralNode.setTextContent(rule.getEntryValue());

                    if (entryFilter != null) {
                        n.appendChild(entryFilter);
                        entryFilter.appendChild(valueReferenceNode);
                        entryFilter.appendChild(fesLiteralNode);
                    }
                // add second filter to document
                } else if ((j == 1) && (i == 0)) {
//                    fesLiteralNode.setTextContent(resultcUnit[1].toString());
                    fesLiteralNode.setTextContent(rule.getExitValue());

                    if (exitFilter != null) {
                        n.appendChild(exitFilter);
                        exitFilter.appendChild(valueReferenceNode);
                        exitFilter.appendChild(fesLiteralNode);
                    }
                }
            }
        }

        // final EML document. Convert document to string for saving in DB
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transormer.transform(source, result);

        eml = result.getWriter().toString();
        finalEml = eml;
        finalEml = finalEml.substring(finalEml.indexOf("<EML"));

        BasicRule basicRule = new BasicRule(rule.getTitle(), "B", "BR2", rule.getDescription(), rule.isPublish(), user.getId(),
                finalEml, false);
        basicRule.setUuid(rule.getUuid());
        return basicRule;
    }

    /**
     * 
     * This method is used to parse an EML file and return a Rule class with rule specific attributes. 
     * The method is called if user want to edit this rule type.
     * 
     * @param basicRule
     * @return {@link Rule}
     */
    public static Rule getRuleByEML(BasicRule basicRule) {
        Rule rule = new Rule();
        rule.setTimeseriesMetadata(basicRule.getTimeseriesMetadata());

        try {
            String eml = basicRule.getEml();
            DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFac.newDocumentBuilder();
            Document doc = docBuilder.parse(new ByteArrayInputStream(eml.getBytes()));

            NodeList filterList = doc.getElementsByTagName(fesFilter);
            Node entryOperatorNode = filterList.item(0);
            String entryFilter = entryOperatorNode.getChildNodes().item(1).getNodeName();
            rule.setEntryOperatorIndex(getSymbolIndexForFilter(entryFilter));

            Node exitOperatorNode = filterList.item(1);
            String exitFilter = exitOperatorNode.getChildNodes().item(1).getNodeName();
            rule.setExitOperatorIndex(getSymbolIndexForFilter(exitFilter));
            
            rule.setEnterEqualsExitCondition(rule.determineEqualEntryExitCondition());
            
            NodeList literalList = doc.getElementsByTagName(fesLiteral);
            Node literalNode = literalList.item(0);
            
            // rValue: Trend value
            rule.setEntryValue(literalNode.getFirstChild().getNodeValue()); 

            // rUnit: Trend value unit. Default value is meter
            rule.setEntryUnit("m");

            literalNode = literalList.item(1);
            
            // cValue: Trend condition value.
            rule.setExitValue(literalNode.getFirstChild().getNodeValue());

            // cUnit. Trend condition value unit. Default unit is meter.
            rule.setExitUnit("m");

            NodeList durationList = doc.getElementsByTagName(duration);
            Node durationNode = durationList.item(0);
            String temp = durationNode.getTextContent();
            temp.substring(2);
            
            // rTime: Time value
            rule.setrTime(temp.substring(2, temp.length()-1));
            
            // rTimeUnit: Time unit 
            rule.setrTimeUnit(temp.substring(temp.length()-1));
            
            // cTime
            // TODO
            
            // cTime unit
            // TODO
            
            // set rule Type
            rule.setRuleType(SimpleRuleType.TENDENCY_OVER_TIME);

        } catch (Exception e) {
            LOGGER.error("Error parsing EML rule", e);
        }

        return rule;
    }
}
