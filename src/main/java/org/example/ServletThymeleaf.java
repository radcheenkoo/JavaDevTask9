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
        templateResolver.setPrefix("src/main/resources/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);
        engine.setTemplateResolver(templateResolver);

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

       String timezone;

        if (req.getParameter("timezone") == null){
            timezone = DEFAULT_TIMEZONE;
        }else {
            timezone = req.getParameter("timezone");


            timezone = URLDecoder.decode(timezone,StandardCharsets.UTF_8);

             /*
            я переставляю пробіл на плюса, бо так чомусь метод decode
            постійно трохи не правильно розкодовував його,без нього не працює.
             */
            timezone = timezone.replace(" ", "+");
        }


        if (timezone == null || timezone.isEmpty()) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (c.getName().equals("lastTimezone")) {
                        timezone = c.getValue();

                        break;
                    }
                }
            }

            if (timezone == null || timezone.isEmpty()) {
                timezone = DEFAULT_TIMEZONE;
            }
        }

        try {

            ZoneId zone = ZoneId.of(timezone);
            ZonedDateTime zdt = ZonedDateTime.now(zone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            String time = zdt.format(formatter);


            Cookie lastTimezoneCookie = new Cookie("lastTimezone", timezone);

            lastTimezoneCookie.setMaxAge(10);
            resp.addCookie(lastTimezoneCookie);


            Map<String, Object> contextVariables = new HashMap<>();
            contextVariables.put("time", time);

            Context context = new Context();
            context.setVariables(contextVariables);


            logger.info("Processing request for timezone: {}", timezone);


            engine.process("show-time-by-UTC", context, resp.getWriter());


        } catch (Exception e) {

            logger.info("Error processing request", e);


            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("<html><body>" +
                    "<h1>Invalid timezone</h1>" +
                    "</body></html>");

        }
    }
}
