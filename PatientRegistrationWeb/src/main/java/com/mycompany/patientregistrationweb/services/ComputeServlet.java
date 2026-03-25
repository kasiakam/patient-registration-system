package com.mycompany.patientregistrationweb.services;

import com.mycompany.patientregistrationweb.model.InvalidPatientDataException;
import com.mycompany.patientregistrationweb.model.Patient;
import com.mycompany.patientregistrationweb.model.PatientRegistry;
import com.mycompany.patientregistartionweb.persistence.PatientEntity;
import com.mycompany.patientregistartionweb.persistence.PatientRepository;
import jakarta.ejb.EJB;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Provides access to the computational part of the model.
 * - GET  /compute  renders statistics computed from {@link PatientRegistry}
 * - POST /compute  validates input and adds a {@link Patient} to the registry
 *
 * Shared model instance is stored in {@link jakarta.servlet.ServletContext}.
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
@WebServlet(name = "ComputeServlet", urlPatterns = {"/compute"})
public class ComputeServlet extends HttpServlet {

    /** Repository used to access the database (patients + operation log). */
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
     * Processes both GET and POST requests (no duplication).
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException I/O error
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String action = request.getParameter("action");
            if (action == null) action = "add";

            if ("update".equalsIgnoreCase(action)) {
                handleUpdatePatient(request, response);
            } else if ("delete".equalsIgnoreCase(action)) {
                handleDeletePatient(request, response);
            } else {
                handleAddPatient(request, response);
            }
            return;
        }

        renderStatistics(request, response);
    }

    /**
     * Adds a new patient (DB + log ADD).
     */
    private void handleAddPatient(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String name = trimOrNull(request.getParameter("name"));
        String surname = trimOrNull(request.getParameter("surname"));
        String ageStr = trimOrNull(request.getParameter("age"));
        String pesel = trimOrNull(request.getParameter("pesel"));
        String gender = trimOrNull(request.getParameter("gender"));

        if (name == null || surname == null || ageStr == null || pesel == null || gender == null) {
            CookieUtils.write(response, "lastError", "missing_data", 3600);
            response.sendRedirect(request.getContextPath() + "/form?error=missing_data");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            CookieUtils.write(response, "lastError", "invalid_age", 3600);
            response.sendRedirect(request.getContextPath() + "/form?error=invalid_age");
            return;
        }

        try {
            // ✅ model validation (PatientData inside Patient)
            new Patient(name, surname, age, pesel, gender);

            // ✅ DB write
            PatientEntity entity = new PatientEntity(name, surname, age, pesel, gender);
            repo.addPatient(entity); // should also log ADD inside repo

            CookieUtils.write(response, "lastPesel", pesel, 3600);
            int opCount = parseIntSafe(CookieUtils.read(request, "opCount")) + 1;
            CookieUtils.write(response, "opCount", String.valueOf(opCount), 3600);
            CookieUtils.write(response, "lastError", "", 3600);

            response.sendRedirect(request.getContextPath() + "/history?success=patient_added");
        } catch (InvalidPatientDataException ex) {
            String encoded = url(ex.getMessage());
            CookieUtils.write(response, "lastError", "model", 3600);
            response.sendRedirect(request.getContextPath() + "/form?error=model&detail=" + encoded);
        } catch (PersistenceException ex) {
            String encoded = url(safeMsg(ex));
            CookieUtils.write(response, "lastError", "db", 3600);
            response.sendRedirect(request.getContextPath() + "/form?error=db&detail=" + encoded);
        } catch (Exception ex) {
            String encoded = url(safeMsg(ex));
            CookieUtils.write(response, "lastError", "server_error", 3600);
            response.sendRedirect(request.getContextPath() + "/form?error=server_error&detail=" + encoded);
        }
    }

    /**
     * Deletes a patient by PESEL (DB + log DELETE).
     */
    private void handleDeletePatient(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String pesel = trimOrNull(request.getParameter("pesel"));
        if (pesel == null) {
            CookieUtils.write(response, "lastError", "missing_data", 3600);
            response.sendRedirect(request.getContextPath() + "/history?error=missing_pesel");
            return;
        }

        try {
            boolean removed = repo.deleteByPesel(pesel); // should also log DELETE

            CookieUtils.write(response, "lastPesel", pesel, 3600);
            int opCount = parseIntSafe(CookieUtils.read(request, "opCount")) + 1;
            CookieUtils.write(response, "opCount", String.valueOf(opCount), 3600);

            response.sendRedirect(request.getContextPath()
                    + "/history?success=" + (removed ? "patient_deleted" : "not_found"));
        } catch (PersistenceException ex) {
            String encoded = url(safeMsg(ex));
            CookieUtils.write(response, "lastError", "db", 3600);
            response.sendRedirect(request.getContextPath() + "/history?error=db&detail=" + encoded);
        } catch (Exception ex) {
            String encoded = url(safeMsg(ex));
            CookieUtils.write(response, "lastError", "server_error", 3600);
            response.sendRedirect(request.getContextPath() + "/history?error=server_error&detail=" + encoded);
        }
    }

    /**
     * Updates a patient (DB + log UPDATE).
     */
    private void handleUpdatePatient(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String oldPesel = trimOrNull(request.getParameter("oldPesel"));
        String name = trimOrNull(request.getParameter("name"));
        String surname = trimOrNull(request.getParameter("surname"));
        String ageStr = trimOrNull(request.getParameter("age"));
        String pesel = trimOrNull(request.getParameter("pesel"));
        String gender = trimOrNull(request.getParameter("gender"));

        if (oldPesel == null || name == null || surname == null || ageStr == null || pesel == null || gender == null) {
            CookieUtils.write(response, "lastError", "missing_data", 3600);
            response.sendRedirect(request.getContextPath() + "/edit?pesel=" + url(oldPesel) + "&error=missing_data");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException ex) {
            CookieUtils.write(response, "lastError", "invalid_age", 3600);
            response.sendRedirect(request.getContextPath() + "/edit?pesel=" + url(oldPesel) + "&error=invalid_age");
            return;
        }

        try {
            // ✅ model validation
            new Patient(name, surname, age, pesel, gender);

            boolean updated = repo.updateByPesel(oldPesel, name, surname, age, pesel, gender); // log UPDATE inside repo
            if (!updated) {
                response.sendRedirect(request.getContextPath() + "/history?error=not_found");
                return;
            }

            CookieUtils.write(response, "lastPesel", pesel, 3600);
            int opCount = parseIntSafe(CookieUtils.read(request, "opCount")) + 1;
            CookieUtils.write(response, "opCount", String.valueOf(opCount), 3600);

            response.sendRedirect(request.getContextPath() + "/history?success=patient_updated");
        } catch (InvalidPatientDataException ex) {
            CookieUtils.write(response, "lastError", "model", 3600);
            response.sendRedirect(request.getContextPath() + "/edit?pesel=" + url(oldPesel) + "&error=model&detail=" + url(ex.getMessage()));
        } catch (PersistenceException ex) {
            CookieUtils.write(response, "lastError", "db", 3600);
            response.sendRedirect(request.getContextPath() + "/edit?pesel=" + url(oldPesel) + "&error=db&detail=" + url(safeMsg(ex)));
        } catch (Exception ex) {
            CookieUtils.write(response, "lastError", "server_error", 3600);
            response.sendRedirect(request.getContextPath() + "/edit?pesel=" + url(oldPesel) + "&error=server_error&detail=" + url(safeMsg(ex)));
        }
    }

    
 /* Renders statistics computed from DB (not from in-memory registry),
 * using the same "green" UI style as History.
 */
private void renderStatistics(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

    String cp = request.getContextPath();

    // optional messages from redirects (if you ever use them)
    String success = request.getParameter("success");
    String error = request.getParameter("error");
    String detail = request.getParameter("detail");

    // cookies
    String lastPesel = CookieUtils.read(request, "lastPesel");
    String opCount = CookieUtils.read(request, "opCount");
    String lastError = CookieUtils.read(request, "lastError");

    try {
        List<PatientEntity> patients = repo.findAllPatients();

        long maleCount = patients.stream().filter(p -> "M".equals(p.getGender())).count();
        long femaleCount = patients.stream().filter(p -> "K".equals(p.getGender())).count();
        long adultCount = patients.stream().filter(p -> p.getAge() >= 18).count();
        int totalCount = patients.size();

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html><head><meta charset='UTF-8'><title>Statistics</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
            out.println("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            out.println("th { background-color: #4CAF50; color: white; }");

            out.println(".success { background: #d4edda; color: #155724; padding: 10px; margin: 10px 0; border-radius: 6px; }");
            out.println(".error { background: #f8d7da; color: #721c24; padding: 10px; margin: 10px 0; border-radius: 6px; }");
            out.println(".info { background: #d1ecf1; color: #0c5460; padding: 10px; margin: 10px 0; border-radius: 6px; }");

            out.println(".nav { margin: 20px 0; }");
            out.println(".btn { padding: 10px 16px; border: none; cursor: pointer; display: inline-block; border-radius: 4px; }");
            out.println(".btn-green { background: #4CAF50; color: white; }");
            out.println(".btn-blue { background: #3498db; color: white; }");
            out.println(".inline { display:inline; }");
            out.println("</style></head><body>");

            out.println("<h1>📊 Patient Statistics (from DB)</h1>");

            // NAV buttons (forms like Delete)
            out.println("<div class='nav'>");
            out.println("<form class='inline' action='" + cp + "/form' method='get'>");
            out.println("<button class='btn btn-green' type='submit'>➕ Add Patient</button>");
            out.println("</form>");

            out.println("<form class='inline' action='" + cp + "/history' method='get' style='margin-left:8px;'>");
            out.println("<button class='btn btn-blue' type='submit'>👥 History</button>");
            out.println("</form>");
            out.println("</div>");

            // SUCCESS/ERROR boxes (optional)
            if (success != null) {
                out.println("<div class='success'>✅ " + html(success) + "</div>");
            }
            if (error != null) {
                out.println("<div class='error'>❌ " + html(error) +
                        (detail != null && !detail.isBlank() ? ("<br><strong>Details:</strong> " + html(detail)) : "") +
                        "</div>");
            }

            // STATS TABLE
            out.println("<h2>Computed values</h2>");
            out.println("<table>");
            out.println("<tr><th>Metric</th><th>Value</th></tr>");
            out.println("<tr><td>Total patients</td><td>" + totalCount + "</td></tr>");
            out.println("<tr><td>Male</td><td>" + maleCount + "</td></tr>");
            out.println("<tr><td>Female</td><td>" + femaleCount + "</td></tr>");
            out.println("<tr><td>Adults (>= 18)</td><td>" + adultCount + "</td></tr>");
            out.println("</table>");

            // COOKIES BOX
            out.println("<h2>🍪 Cookies (shown to the client)</h2>");
            out.println("<div class='info'>");
            out.println("<strong>lastPesel:</strong> " + html(noneIfBlank(lastPesel)) + "<br>");
            out.println("<strong>opCount:</strong> " + html(noneIfBlank(opCount)) + "<br>");
            out.println("<strong>lastError:</strong> " + html(noneIfBlank(lastError)));
            out.println("</div>");

            out.println("</body></html>");
        }

    } catch (PersistenceException ex) {
        String encoded = url(safeMsg(ex));
        CookieUtils.write(response, "lastError", "db", 3600);
        response.sendRedirect(cp + "/form?error=db&detail=" + encoded);
    } catch (Exception ex) {
        String encoded = url(safeMsg(ex));
        CookieUtils.write(response, "lastError", "server_error", 3600);
        response.sendRedirect(cp + "/form?error=server_error&detail=" + encoded);
    }
}

    private String trimOrNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private int parseIntSafe(String raw) {
        if (raw == null) return 0;
        try { return Integer.parseInt(raw); } catch (NumberFormatException ex) { return 0; }
    }

    private String url(String s) {
        if (s == null) s = "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private String safeMsg(Exception ex) {
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? ex.getClass().getSimpleName() : m;
    }

    private String html(String s) {
        if (s == null) return "(none)";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    
    private String noneIfBlank(String s) {
    if (s == null || s.isBlank()) return "(none)";
    return s;
}
}
