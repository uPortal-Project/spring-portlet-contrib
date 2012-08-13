<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<jsp:directive.include file="/WEB-INF/jsp/include.jsp"/>

<h2>Hello ${ fn:escapeXml(username) }!</h2>

<p>${renderRequest.parameterMap}</p>

<c:if test="${not empty userDetails}">
    <ul>
        <li>enabled - ${userDetails.enabled}</li>
        <li>credentialsNonExpired - ${userDetails.credentialsNonExpired}</li>
        <li>accountNonLocked - ${userDetails.accountNonLocked}</li>
        <li>accountNonExpired - ${userDetails.accountNonExpired}</li>
        <li>password - ${userDetails.password}</li>
        <li>GrantedAuthorities
            <ul>
                <c:forEach var="auth" items="${userDetails.authorities}">
                    <li>${auth.authority}</li>
                </c:forEach>
            </ul>
        </li>
    </ul>
</c:if>

<portlet:renderURL var="defaultUrl" />
<a href="${defaultUrl}">Default</a>

<portlet:renderURL var="preAuthEveryoneUrl">
    <portlet:param name="preAuth" value="everyone"/>
</portlet:renderURL>
<a href="${preAuthEveryoneUrl}">PreAuth Everyone</a>

<portlet:renderURL var="preAuthAdminUrl">
    <portlet:param name="preAuth" value="admin"/>
</portlet:renderURL>
<a href="${preAuthAdminUrl}">PreAuth Admin</a>
