<%@ page language="java" %>

<%@ taglib tagdir="/WEB-INF/tags" prefix="tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://metawidget.org/html" prefix="m"%>
<%@ taglib uri="http://metawidget.org/example/jsp/addressbook" prefix="a"%>

<tags:page>

	<c:choose>
		<c:when test="${contact.class.simpleName == 'Personalcontact'}">
			<div id="page-image">
				<img src="media/personal.gif">
			</div>
			<div id="content">				
			<h1>Personal Contact</h1>
		</c:when>
		<c:otherwise>
			<div id="page-image">
				<img src="media/business.gif">
			</div>
			<div id="content">				
			<h1>Business Contact</h1>
		</c:otherwise>
	</c:choose>

		<html:form action="/save">

			<m:metawidget property="contact" readOnly="${contact.readOnly}">
				<m:param name="tableStyleClass" value="table-form"/>
				<m:param name="columnStyleClasses" value="table-label-column,table-component-column,required"/>
				<m:param name="sectionStyleClass" value="section-heading"/>

				<m:stub property="communications">
					<input type="hidden" name="deleteCommunicationId" id="deleteCommunicationId"/>
					<table class="data-table">
						<thead>
							<tr>
								<th class="column-half">Type</th>
								<th class="column-half">Value</th>
								<th class="column-tiny">&nbsp;</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${a:sort(contact.communications)}" var="_communication">
								<tr>
									<td class="column-half">${_communication.type}</td>
									<td class="column-half">${_communication.value}</td>
									<td class="column-tiny, table-buttons">
										<c:if test="${!contact.readOnly}">
											<input type="submit" name="deleteCommunication" value="Delete" onClick="if ( !confirm( 'Are you sure you want to delete this communication?' )) return false; document.getElementById( 'deleteCommunicationId' ).value = '${_communication.id}'"/>
										</c:if>
									</td>
								</tr>
							</c:forEach>
						</tbody>
						<c:if test="${!contact.readOnly}">
							<tfoot>
								<tr>
									<jsp:useBean id="communication" class="org.metawidget.example.shared.addressbook.model.Communication"/>						
									<td class="column-half"><mh:metawidget value="communication.type" style="width: 100%" layoutClass=""/></td>
									<td class="column-half"><mh:metawidget value="communication.value" style="width: 100%" layoutClass=""/></td>
									<td class="column-tiny, table-buttons"><input type="submit" name="addCommunication" value="Add"/></td>
								</tr>
							</foot>
						</c:if>
					</table>
				</m:stub>

				<m:facet name="buttons" styleClass="buttons">
					<c:choose>
						<c:when test="${contact.readOnly}">
							<html:submit property="edit">
								<bean:message key="edit"/>
 							</html:submit>
							<html:submit property="cancel">
								<bean:message key="back"/>
		 					</html:submit>								
						</c:when>
						<c:otherwise>
							<html:submit property="save">
								<bean:message key="save"/>
 							</html:submit>								
							<html:submit property="delete" onclick="if ( !confirm( 'Sure you want to delete this contact?' )) return false">
								<bean:message key="delete"/>
 							</html:submit>
							<html:submit property="cancel">
								<bean:message key="cancel"/>
		 					</html:submit>								
 						</c:otherwise>
 					</c:choose> 							
				</m:facet>

			</m:metawidget>

		</html:form>
	
	</div>

</tags:page>