<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="socialTags" tagdir="/WEB-INF/tags" %>

<jsp:include page="parts/header.jsp">
    <jsp:param name="title" value="Profile" />
</jsp:include>

<div class="container">
    <div class="row">
        <div class="col-md-3">
            <jsp:include page="parts/userMenu.jsp" />
        </div>
        <div class="col-md-9">
            <div class="friends-top">
                <form action="./friends.html" method="get">
                    <div class="input-group stylish-input-group">
                        <input type="text" class="form-control" name="search" placeholder="Search">
                        <span class="input-group-addon">
                        <button type="submit">
                            <span class="glyphicon glyphicon-search"></span>
                        </button>
                    </span>
                    </div>
                </form>
            </div>

            <div class="friends-top">
                <h3>Friends requests</h3>
                <c:if test="${not empty friendsRequests}">
                    <table class="table">
                        <c:forEach var="friendRequest" items="${friendsRequests}">
                            <tr>
                                <td>
                                    <socialTags:Avatar user="${friendRequest.friend}"/>
                                </td>
                                <td>
                                    <a href="/conversation?companion=${friendRequest.friend.id}" class="btn btn-info">Message</a>
                                </td>
                                <td>
                                    <socialTags:friendsActionForm user="${friendRequest.friend}" button="success" action="accept" name="Accept" />
                                </td>
                                <td>
                                    <socialTags:friendsActionForm user="${friendRequest.friend}" button="danger" action="decline" name="Decline" />
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:if>
                <c:if test="${empty friendsRequests}">
                    <h4>No requests yet...</h4>
                </c:if>
            </div>

            <div class="friends-top">
                <h3>Your requests</h3>
                <c:if test="${not empty usersRequests}">
                    <table class="table">
                        <c:forEach var="userRequest" items="${usersRequests}">
                            <tr>
                                <td>
                                    <socialTags:Avatar user="${userRequest.friend}"/>
                                </td>
                                <td>
                                    <a href="/conversation?companion=${userRequest.friend.id}" class="btn btn-info">Message</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:if>
                <c:if test="${empty usersRequests}">
                    <h4>No requests yet...</h4>
                </c:if>
            </div>

            <div class="friends-top">
                <h3>Your friends</h3>
                <c:if test="${not empty friends}">
                    <table class="table">
                        <c:forEach var="friend" items="${friends}">
                            <tr>
                                <td>
                                    <socialTags:Avatar user="${friend.friend}"/>
                                </td>
                                <td>
                                    <a href="/conversation?companion=${friend.friend.id}" class="btn btn-info">Message</a>
                                </td>
                                <td>
                                    <socialTags:friendsActionForm user="${friend.friend}" button="danger" action="remove" name="Remove" />
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:if>
                <c:if test="${empty friends}">
                    <h4>No friends yet...</h4>
                </c:if>
            </div>
        </div>
    </div>
</div>

<jsp:include page="parts/footer.jsp" >
    <jsp:param name="specificScript" value="js/profile.js" />
</jsp:include>