package org.ecocean.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ecocean.ContextConfiguration;
import org.ecocean.Global;
import org.ecocean.ShepherdProperties;
import org.ecocean.html.HtmlConfig;
import org.ecocean.rest.SimpleUser;
import org.ecocean.security.User;
import org.ecocean.util.Jade4JUtils;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.samsix.database.ConnectionInfo;
import com.samsix.database.Database;

public class ServletUtils {
//    private static Logger logger = LoggerFactory.getLogger(ServletUtils.class);

    private static final String DEFAULT_LANG_CODE = "en";

    private ServletUtils() {
        // prevent instantiation
    }

    public static SimpleUser getUser(final HttpServletRequest request) {
        User user = Global.INST.getUserService().getUserById(request.getRemoteUser());
        if (user == null) {
            return null;
        }

        return user.toSimple();
    }

    /**
     * At present this just returns our one db connection. I'm wrapping it in this class
     * so that in the future we could have a different db per context and then we can
     * just return that db as we would get the context from the HttpServletRequest.
     *
     * @param request
     * @return
     */
    public static Database getDb(final ServletRequest request) {
        return Global.INST.getDb();
    }

    public static ConnectionInfo getConnectionInfo(final ServletRequest request) {
        return Global.INST.getConnectionInfo();
    }

    public static String getContext(final HttpServletRequest request) {
        Properties contexts = ShepherdProperties.getContextsProperties();

        //check the URL for the context attribute
        //this can be used for debugging and takes precedence
        if (request.getParameter("context") != null) {
          //get the available contexts
          if (contexts.containsKey((request.getParameter("context") + "DataDir"))) {
            return request.getParameter("context");
          }
        }

        //the request cookie is the next thing we check. this should be the primary means of figuring context out
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("wildbookContext".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        //finally, we will check the URL vs values defined in context.properties to see if we can set the right context
        String currentURL=request.getServerName();
        for (int q=0; q < contexts.size(); q++) {
            String thisContext="context"+q;
            ArrayList<String> domainNames = ContextConfiguration.getContextDomainNames(thisContext);
            int numDomainNames=domainNames.size();
            for (int p=0;p<numDomainNames;p++) {
                if (currentURL.indexOf(domainNames.get(p)) != -1) {
                    return thisContext;
                }
            }
        }
        return ContextConfiguration.getDefaultContext();
    }


    public static String getLanguageCode(final HttpServletRequest request) {
        List<String> languages = Global.INST.getAppResources().getStringList("languages", Collections.emptyList());

        //if specified directly, always accept the override
        String langCode = request.getParameter("langCode");
        if (langCode != null && languages.contains(langCode)) {
            return langCode;
        }

        //the request cookie is the next thing we check. this should be the primary means of figuring langCode out
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies){
                if ("wildbookLangCode".equals(cookie.getName())) {
                    if (languages.contains(cookie.getValue())) {
                        return cookie.getValue();
                    }
                }
            }
        }

        //
        // TODO: finally, we will check the URL vs values defined in context.properties to see if we can set the right context
        // - future - detect browser supported language codes and locale from the HTTPServletRequest object

        return Global.INST.getAppResources().getString("language.default", DEFAULT_LANG_CODE);
    }


    private static String renderError(final HttpServletRequest request, final Throwable ex) {
        if (ex == null) {
            return "<NULL>";
        }

        Map<String, Object> map = null;
        map = new HashMap<String, Object>();
        map.put("message", ex.getMessage());
        map.put("stack", ExceptionUtils.getStackTrace(ex));

        try {
            return Jade4JUtils.renderFile(request.getServletContext().getRealPath("/jade/error"), map);
        } catch (Throwable ex2) {
            ex2.printStackTrace();
            return ex2.getMessage();
        }
    }


    /**
     *
     * @param request
     * @param jadeFile can have slashes in it to indicate sub-directories
     * @return
     */
    public static String renderJade(final HttpServletRequest request, final String jadeFile) {
        return renderJade(request, jadeFile, null);
    }

    /**
     *
     * @param request
     * @param jadeFile can have slashes in it to indicate sub-directories
     * @return
     */
    public static String renderJade(final HttpServletRequest request,
                                    final String jadeFile,
                                    final Map<String, Object> vars) {
        try {
            return Jade4JUtils.renderFile(request.getServletContext().getRealPath("/jade/" + jadeFile), vars);
        } catch (Throwable ex) {
            return renderError(request, ex);
        }
    }

    public static String getURLLocation(final HttpServletRequest request) {
        return request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
      }

    public static HtmlConfig getHtmlConfig() throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:webapp_config.yml");

        Yaml yaml = new Yaml(new Constructor(HtmlConfig.class));
        return (HtmlConfig) yaml.load(new FileReader(file));
    }
}