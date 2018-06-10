<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<h2>Hello ${ fn:escapeXml(username) }!</h2>

<p>handler=${handler}</p>

<p><font size='5'>
   All Request Parameters:
</font><p>

<c:forEach var='parameter' items='${paramValues}'> 
   <ul>
      <li><b><c:out value='${parameter.key}'/></b>:</li>
      <c:forEach var='value' items='${parameter.value}'>
         <c:out value='${value}'/>   
      </c:forEach>
   </ul>
</c:forEach>

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

<c:url var="defaultUrl" value="index.html"/>
<a href="${defaultUrl}">Default</a>

<c:url var="everyoneUrl" value="index.html">
    <c:param name="preAuth" value="everyone" />
</c:url>
<a href="${everyoneUrl}">PreAuth Everyone</a>

<c:url var="adminUrl" value="index.html">
    <c:param name="preAuth" value="admin" />
</c:url>
<a href="${adminUrl}">PreAuth Admin</a>
