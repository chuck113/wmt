package com.where.web;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import java.io.PrintWriter;
import java.io.IOException;

public class TestServlet extends HttpServlet {

    Logger LOG = Logger.getLogger(TestServlet.class);
    /**
     * Method to receive get requests from the web server
     * (Passes them onto the doPost method)
     *
     * @param req The HttpServletRequest which contains the information submitted via get
     * @param res A response containing the required response data for this request
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doPost(req, res);              //  Pass all GET request to the the doPost method
    }

    /**
     * Method to relieve and process Post requests from the web server
     *
     * @param req The HttpServletRequest which contains the information submitted via post
     * @param res A response containing the required response data for this request
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        LOG.info("doing post, query is "+req.getQueryString());
        res.setContentType("text/html");    //  Set the content type of the response
        PrintWriter out = res.getWriter();    //  PrintWriter to write text to the response
        out.println("Hello World");        //  Write Hello World
        out.close();            //  Close the PrintWriter
    }
}

