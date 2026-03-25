package com.mycompany.patientregistrationweb.services;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
/**
 * Renders the patient form as HTML (no JavaScript) and displays validation/model errors.
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
/**
 * Renders the patient form as HTML (no JavaScript) and displays validation/model/database errors.
 *
 * The form submits to /compute (POST) and the data is persisted in the database.
 * The patient list and operation history are available on /history.
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
@WebServlet(name = "FormServlet", urlPatterns = {"/form"})
public class FormServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String error = request.getParameter("error");
        String detail = request.getParameter("detail");

        // decode optional details (ComputeServlet sends URL-encoded text)
        String decodedDetail = decode(detail);

        String cp = request.getContextPath();

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Add Patient</title>");

            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; max-width: 600px; }");
            out.println(".form-group { margin: 15px 0; }");
            out.println("label { display: block; margin-bottom: 5px; font-weight: bold; }");
            out.println("input, select { width: 100%; padding: 8px; margin: 5px 0; }");
            out.println(".error { color: #721c24; margin: 10px 0; padding: 10px; background: #f8d7da; border-radius: 6px; }");
            out.println(".info { color: #0c5460; margin: 10px 0; padding: 10px; background: #d1ecf1; border-radius: 6px; }");
            out.println(".nav { margin: 20px 0; }");
            out.println(".btn { padding: 10px 20px; border: none; cursor: pointer; text-decoration: none; display: inline-block; margin-right: 10px; border-radius: 4px; }");
            out.println(".btn-green { background: #4CAF50; color: white; }");
            out.println(".btn-gray { background: #ccc; color: black; }");
            out.println("</style>");

            out.println("</head><body>");

            out.println("<h1>Add New Patient</h1>");

            // Clear info about where data is displayed (requirement)
            out.println("<div class='info'>");
            out.println("<strong>Info:</strong> This form adds a patient. The saved data is displayed on ");
            out.println("<strong>History</strong> (Database + Cookies) and statistics on <strong>Statistics</strong>.");
            out.println("</div>");

            // NAVIGATION
            out.println("<div class='nav'>");
            out.println("<a class='btn btn-gray' href='" + cp + "/history'>Back to Patient List</a>");
            out.println("<a class='btn btn-green' href='" + cp + "/compute'>View Statistics</a>");
            out.println("</div>");

            // ERROR BOX
            if (error != null) {
                out.println("<div class='error'>");
                out.println("<strong>Error:</strong> " + html(mapError(error)));
                if (decodedDetail != null && !decodedDetail.isBlank()) {
                    out.println("<br><strong>Details:</strong> " + html(decodedDetail));
                }
                out.println("</div>");
            }

            // FORM
            out.println("<form action='" + cp + "/compute' method='post'>");
            out.println("<input type='hidden' name='action' value='add'>");

            // NAME
            out.println("<div class='form-group'>");
            out.println("<label for='name'>Name *</label>");
            out.println(
                    "<input type='text' id='name' name='name' required " +
                            "pattern=\"[A-Za-zĄĆĘŁŃÓŚŹŻąćęłńóśźż\\-\\' ]{2,50}\" " +
                            "title='Only letters allowed (no digits). 2–50 characters.' " +
                            "placeholder='Enter first name'>"
            );
            out.println("</div>");

            // SURNAME
            out.println("<div class='form-group'>");
            out.println("<label for='surname'>Surname *</label>");
            out.println(
                    "<input type='text' id='surname' name='surname' required " +
                            "pattern=\"[A-Za-zĄĆĘŁŃÓŚŹŻąćęłńóśźż\\-\\' ]{2,50}\" " +
                            "title='Only letters allowed (no digits). 2–50 characters.' " +
                            "placeholder='Enter last name'>"
            );
            out.println("</div>");

            // AGE
            out.println("<div class='form-group'>");
            out.println("<label for='age'>Age *</label>");
            out.println("<input type='number' id='age' name='age' required min='1' max='130' placeholder='Enter age (1–130)'>");
            out.println("</div>");

            // PESEL
            out.println("<div class='form-group'>");
            out.println("<label for='pesel'>PESEL *</label>");
            out.println("<input type='text' id='pesel' name='pesel' required pattern='[0-9]{11}' maxlength='11' placeholder='Enter 11-digit PESEL'>");
            out.println("</div>");

            // GENDER
            out.println("<div class='form-group'>");
            out.println("<label for='gender'>Gender *</label>");
            out.println("<select id='gender' name='gender' required>");
            out.println("<option value=''>Select gender</option>");
            out.println("<option value='M'>Male</option>");
            out.println("<option value='K'>Female</option>");
            out.println("</select>");
            out.println("</div>");

            // BUTTONS
            out.println("<div class='form-group'>");
            out.println("<button type='submit' class='btn btn-green'>Save Patient</button>");
            out.println("<a href='" + cp + "/history' class='btn btn-gray'>Cancel</a>");
            out.println("</div>");

            out.println("</form>");
            out.println("</body></html>");
        }
    }

    /**
     * Maps error codes to user-friendly messages.
     *
     * @param error error code
     * @return user-friendly message
     */
    private String mapError(String error) {
        return switch (error) {
            case "missing_data" -> "All fields are required.";
            case "invalid_age" -> "Age must be a number between 1 and 130.";
            case "model" -> "Invalid patient data (e.g. duplicate PESEL or invalid name/surname).";
            case "db" -> "Database error.";
            case "server_error" -> "Unexpected server error.";
            default -> error;
        };
    }

    /**
     * Decodes URL-encoded detail text.
     *
     * @param s encoded string
     * @return decoded string or null
     */
    private String decode(String s) {
        if (s == null) return null;
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return s;
        }
    }

    /**
     * Minimal HTML escaping.
     *
     * @param s input string
     * @return escaped string
     */
    private String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}