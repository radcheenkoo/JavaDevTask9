package org.example;

import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;


@WebServlet("/time")
public class ServletThymeleaf extends HttpServlet {

    private static final String DEFAULT_TIMEZONE = "UTC";
    private static final Logger logger = LoggerFactory.getLogger(ServletThymeleaf.class);
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        FileTemplateResolver templateResolver = new FileTemplateResolver();
        // templateResolver.setPrefix("D:\\JavaOnlineGoIT\\JavaDevTask9\\src\\main\\webapp\\WEB-INF\\templates\\");
        templateResolver.setPrefix("src/main/webapp/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);
        engine.setTemplateResolver(templateResolver);

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String timezone = getTimezone(req);

        timezone = URLDecoder.decode(timezone,StandardCharsets.UTF_8);
        timezone = timezone.replace(" ", "+");


        String time = "";

        try {

            ZoneId zone = ZoneId.of(timezone);
            ZonedDateTime zdt = ZonedDateTime.now(zone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            time = zdt.format(formatter);

        }catch (DateTimeException e) {

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("<html><body>" +
                    "<h1>Invalid timezone</h1>" +
                    "</body></html>");
        }


        if (!timezone.equals(DEFAULT_TIMEZONE)){

            Cookie lastTimezoneCookie = new Cookie("lastTimezone", timezone);
            lastTimezoneCookie.setMaxAge(10);
            resp.addCookie(lastTimezoneCookie);

        }



            Map<String, Object> contextVariables = new HashMap<>();
            contextVariables.put("time", time);

            Context context = new Context();
            context.setVariables(contextVariables);


            logger.info("Processing request for timezone: {}", timezone);


            engine.process("time", context, resp.getWriter());

    }

    private String getTimezone(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for (Cookie c: cookies) {
                if (c.getName().equals("lastTimezone")) {

                    System.out.println(c.getValue());

                    return c.getValue();
                }
            }
        }

        if (request.getParameter("timezone") == null) {
            return DEFAULT_TIMEZONE;
        }

        if (!request.getParameter("timezone").isEmpty()) {
            return request.getParameter("timezone");
        }

        return DEFAULT_TIMEZONE;

    }
}
