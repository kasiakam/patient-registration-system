package com.mycompany.patientregistrationweb.services;

import com.mycompany.patientregistartionweb.persistence.PatientEntity;
import com.mycompany.patientregistartionweb.persistence.PatientRepository;
import com.mycompany.patientregistrationweb.model.Patient;
import com.mycompany.patientregistrationweb.model.PatientRegistry;
import jakarta.ejb.EJB;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Renders an HTML form for editing an existing patient (no JavaScript).
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
@WebServlet(name = "EditServlet", urlPatterns = {"/edit"})
public class EditServlet extends HttpServlet {

    /** Database repository. */
    @EJB
    private PatientRepository repo;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles GET/POST without duplication and renders edit form.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException I/O error
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String cp = request.getContextPath();

        String pesel = trimOrNull(request.getParameter("pesel"));
        String error = request.getParameter("error");
        String detail = decode(request.getParameter("detail"));

        if (pesel == null) {
            response.sendRedirect(cp + "/history?error=missing_pesel");
            return;
        }

        try {
            PatientEntity patient = repo.findByPesel(pesel);

            if (patient == null) {
                response.sendRedirect(cp + "/history?error=not_found");
                return;
            }

            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html><head><meta charset='UTF-8'><title>Edit Patient</title>");
                out.println("<style>");
                out.println("body { font-family: Arial, sans-serif; margin: 40px; max-width: 600px; }");
                out.println(".form-group { margin: 15px 0; }");
                out.println("label { display: block; margin-bottom: 5px; font-weight: bold; }");
                out.println("input, select { width: 100%; padding: 8px; margin: 5px 0; }");
                out.println(".error { color: red; margin: 10px 0; padding: 10px; background: #f8d7da; }");
                out.println(".nav { margin: 20px 0; }");
                out.println(".btn { padding: 10px 20px; border: none; cursor: pointer; text-decoration: none; display: inline-block; margin-right: 10px; }");
                out.println(".btn-green { background: #4CAF50; color: white; }");
                out.println(".btn-gray { background: #ccc; color: black; }");
                out.println("</style></head><body>");

                out.println("<h1>Edit Patient</h1>");

                out.println("<div class='nav'>");
                out.println("<a class='btn btn-gray' href='" + cp + "/history'>Back to Patient List</a>");
                out.println("<a class='btn btn-green' href='" + cp + "/compute'>Statistics</a>");
                out.println("</div>");

                if (error != null) {
                    out.println("<div class='error'>");
                    out.println("<strong>Error:</strong> " + html(error));
                    if (detail != null && !detail.isBlank()) {
                        out.println("<br><strong>Details:</strong> " + html(detail));
                    }
                    out.println("</div>");
                }

                // IMPORTANT: update goes to /compute
                out.println("<form action='" + cp + "/compute' method='post'>");
                out.println("<input type='hidden' name='action' value='update'>");
                out.println("<input type='hidden' name='oldPesel' value='" + html(patient.getPesel()) + "'>");

                out.println("<div class='form-group'>");
                out.println("<label for='name'>Name *</label>");
                out.println("<input type='text' id='name' name='name' required " +
                        "pattern=\"[A-Za-zĄĆĘŁŃÓŚŹŻąćęłńóśźż\\-\\' ]{2,50}\" " +
                        "value='" + html(patient.getName()) + "'>");
                out.println("</div>");

                out.println("<div class='form-group'>");
                out.println("<label for='surname'>Surname *</label>");
                out.println("<input type='text' id='surname' name='surname' required " +
                        "pattern=\"[A-Za-zĄĆĘŁŃÓŚŹŻąćęłńóśźż\\-\\' ]{2,50}\" " +
                        "value='" + html(patient.getSurname()) + "'>");
                out.println("</div>");

                out.println("<div class='form-group'>");
                out.println("<label for='age'>Age *</label>");
                out.println("<input type='number' id='age' name='age' required min='1' max='130' value='" + patient.getAge() + "'>");
                out.println("</div>");

                out.println("<div class='form-group'>");
                out.println("<label for='pesel'>PESEL *</label>");
                out.println("<input type='text' id='pesel' name='pesel' required pattern='[0-9]{11}' maxlength='11' value='" + html(patient.getPesel()) + "'>");
                out.println("</div>");

                out.println("<div class='form-group'>");
                out.println("<label for='gender'>Gender *</label>");
                out.println("<select id='gender' name='gender' required>");
                out.println("<option value='M'" + ("M".equals(patient.getGender()) ? " selected" : "") + ">Male</option>");
                out.println("<option value='K'" + ("K".equals(patient.getGender()) ? " selected" : "") + ">Female</option>");
                out.println("</select>");
                out.println("</div>");

                out.println("<div class='form-group'>");
                out.println("<button type='submit' class='btn btn-green'>Save changes</button>");
                out.println("<a class='btn btn-gray' href='" + cp + "/history'>Cancel</a>");
                out.println("</div>");

                out.println("</form>");
                out.println("</body></html>");
            }

        } catch (PersistenceException ex) {
            response.sendRedirect(cp + "/history?error=db");
        } catch (Exception ex) {
            response.sendRedirect(cp + "/history?error=server_error");
        }
    }

    private String trimOrNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private String decode(String s) {
        if (s == null) return null;
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
