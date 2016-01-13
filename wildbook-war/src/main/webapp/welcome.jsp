<%@ page contentType="text/html; charset=utf-8" language="java"
         import="
         org.ecocean.ShepherdProperties,
         org.ecocean.servlet.ServletUtils,
         org.ecocean.security.User,
         org.ecocean.rest.UserController,
         java.util.Properties,
         org.slf4j.Logger,
         org.slf4j.LoggerFactory,
         org.apache.commons.lang3.StringEscapeUtils" %>

<%
String context = ServletUtils.getContext(request);


  //handle some cache-related security
  response.setHeader("Cache-Control", "no-cache"); //Forces caches to obtain a new copy of the page from the origin server
  response.setHeader("Cache-Control", "no-store"); //Directs caches not to store the page under any circumstance
  response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0 backward compatibility


  //setup our Properties object to hold all properties
  //String langCode = "en";
  String langCode=ServletUtils.getLanguageCode(request);

  //set up the file input stream
  Properties props = new Properties();
  //props.load(getClass().getResourceAsStream("/bundles/" + langCode + "/welcome.properties"));
  props = ShepherdProperties.getProperties("welcome.properties", langCode, context);

  session = request.getSession(true);
  session.setAttribute("logged", "true");
  if ((request.getParameter("reflect") != null)) {
    response.sendRedirect(request.getParameter("reflect"));
  }
  
   Logger logger = LoggerFactory.getLogger(getClass());
   if (logger.isInfoEnabled()) {
      logger.info(request.getRemoteUser() + " logged in from IP address " + request.getRemoteAddr() + ".");
   }
   
   User user = ServletUtils.getUser(request);
%>
<jsp:include page="header.jsp" flush="true"/>

<div class="container maincontent">
    <h1 class="intro"><%=props.getProperty("loginSuccess")%></h1>

    <p><%=props.getProperty("loggedInAs")%>
        <strong><%=user.toSimple().getDisplayName()%></strong>.
    </p>

    <p><%=props.getProperty("grantedRole")%><br/>
        <em><%=UserController.getAllRolesForUserAsString(request, user.getId()).replaceAll("\r","<br/>")%></em>
    </p>
    <p><%=props.getProperty("pleaseChoose")%></p>
    <p>&nbsp;</p>
</div>

<jsp:include page="footer.jsp" flush="true"/>