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
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;


@WebServlet("/thymeleafServlet")
public class ServletThymeleaf extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ServletThymeleaf.class);
    private TemplateEngine engine;

    @Override
    public void init() {
        engine = new TemplateEngine();

        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix("D:\\JavaOnlineGoIT\\JavaDevTask9\\src\\main\\resources\\templates\\");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCacheable(false);
        engine.setTemplateResolver(templateResolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String timezone = req.getQueryString();

        timezone = timezone.substring(9,timezone.length());

        URLDecoder.decode(timezone);

        if (timezone == null || timezone.isEmpty()) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null){
                for (Cookie c: cookies) {
                    if (c.getName().equals("lastTimezone")){
                        timezone = c.getValue();
                        break;
                    }
                }
            }
            else if(timezone == null){
                timezone = "UTC";
            }
        }else {

            Cookie lastTimezoneCookie = new Cookie("lastTimezone",timezone);

            lastTimezoneCookie.setMaxAge(60 * 30);
            resp.addCookie(lastTimezoneCookie);
        }



        try {
            ZoneId zone = ZoneId.of(timezone);
            ZonedDateTime zdt = ZonedDateTime.now(zone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            String time = zdt.format(formatter);

            System.out.println(time);


            Map<String, Object> contextVariables = new HashMap<>();
            contextVariables.put("time", time);

            Context context = new Context();
            context.setVariables(contextVariables);


            logger.info("Processing request for timezone: {}", timezone);


            engine.process("show-time-by-UTC", context,resp.getWriter());


        } catch (DateTimeException e) {

            logger.info("Error processing request", e);

            
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("<html><body><h1>Invalid timezone</h1></body></html>");

        }
    }
}
