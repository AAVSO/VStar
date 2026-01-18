/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.auth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.ui.resources.LoginInfo;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class adds AAVSO observer code and membership status to a LoginInfo
 * object.
 * 
 * See Auth0JSONAutheticationSource
 */
@Deprecated
public class VSXWebServiceMemberInfo {

    /**
     * Given an AAVSO user ID, set observer code and membership status on the
     * supplied LoginInfo object.
     * 
     * @param userID The AAVSO user ID.
     * @param info   The LoginInfo object to be modified.
     */
    public void retrieveUserInfo(String userID, LoginInfo info)
            throws MalformedURLException, ParserConfigurationException, SAXException, IOException {

        URL vsxUrl = new URL(ResourceAccessor.getVsxApiUrlBase() + "api.member&id=" + userID);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(vsxUrl.openStream());

        document.getDocumentElement().normalize();

        NodeList obsCodeNodes = document.getElementsByTagName("ObsCode");
        if (obsCodeNodes.getLength() == 1) {
            info.setObserverCode(obsCodeNodes.item(0).getTextContent());

            NodeList memberNodes = document.getElementsByTagName("MemberBenefits");
            if (memberNodes.getLength() == 1) {
                String memberVal = memberNodes.item(0).getTextContent();
                boolean isMember = Boolean.parseBoolean(memberVal);
                info.setMember(isMember);
            }
        }
    }
}
