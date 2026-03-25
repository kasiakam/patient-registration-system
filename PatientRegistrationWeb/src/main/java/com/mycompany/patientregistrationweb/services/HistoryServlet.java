package com.mycompany.patientregistrationweb.services;

import com.mycompany.patientregistartionweb.persistence.OperationLogEntity;
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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Provides access to the history/data stored in the model collection.
 * Renders the patient list and shows cookie values to the client.
 *
 * - GET  /history renders patient list
 * - POST /history delegates to the same handler (no duplication)
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {

    /** Repository used to access the database. */
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

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String cp = request.getContextPath();

        String success = request.getParameter("success");
        String error = request.getParameter("error");
        String detail = decode(request.getParameter("detail"));

        // cookies
        String lastPesel = CookieUtils.read(request, "lastPesel");
        String opCount = CookieUtils.read(request, "opCount");
        String lastError = CookieUtils.read(request, "lastError");

        try {
            List<PatientEntity> patients = repo.findAllPatients();
            List<OperationLogEntity> ops = repo.findAllOperations();

            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html><head><meta charset='UTF-8'><title>History</title>");
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
                out.println(".btn-gray { background: #ccc; color: black; text-decoration:none; }");
                out.println(".btn-red { background: #e74c3c; color: white; }");
                out.println(".btn-blue { background: #3498db; color: white; }");
                out.println(".btn + .btn { margin-left: 8px; }");
                out.println(".inline { display:inline; }");
                out.println("</style></head><body>");

                out.println("<h1>👥 Patient Management System</h1>");

                // NAV buttons (as forms like Delete)
                out.println("<div class='nav'>");
                out.println("<form class='inline' action='" + cp + "/form' method='get'>");
                out.println("<button class='btn btn-green' type='submit'>➕ Add Patient</button>");
                out.println("</form>");

                out.println("<form class='inline' action='" + cp + "/compute' method='get'>");
                out.println("<button class='btn btn-blue' type='submit'>📊 Statistics</button>");
                out.println("</form>");
                out.println("</div>");

                // SUCCESS/ERROR from query params
                if (success != null) {
                    out.println("<div class='success'>✅ " + html(success) + "</div>");
                }
                if (error != null) {
                    out.println("<div class='error'>❌ " + html(error) +
                            (detail != null && !detail.isBlank() ? ("<br><strong>Details:</strong> " + html(detail)) : "") +
                            "</div>");
                }

                // COOKIES section (clear info: cookies vs DB)
                out.println("<h2>🍪 Cookies (shown to the client)</h2>");
                out.println("<div class='info'>");
                out.println("<strong>lastPesel:</strong> " + html(noneIfBlank(lastPesel)) + "<br>");
                out.println("<strong>opCount:</strong> " + html(noneIfBlank(opCount)) + "<br>");
                out.println("<strong>lastError:</strong> " + html(noneIfBlank(lastError)));
                out.println("</div>");

                // DB PATIENTS
                out.println("<h2>🗄️ Database: Patients (" + patients.size() + ")</h2>");
                if (patients.isEmpty()) {
                    out.println("<p>No patients in database.</p>");
                } else {
                    out.println("<table>");
                    out.println("<tr><th>#</th><th>Name</th><th>Surname</th><th>Age</th><th>PESEL</th><th>Gender</th><th>Actions</th></tr>");

                    int i = 1;
                    for (PatientEntity p : patients) {
                        out.println("<tr>");
                        out.println("<td>" + (i++) + "</td>");
                        out.println("<td>" + html(p.getName()) + "</td>");
                        out.println("<td>" + html(p.getSurname()) + "</td>");
                        out.println("<td>" + p.getAge() + "</td>");
                        out.println("<td>" + html(p.getPesel()) + "</td>");
                        out.println("<td>" + html(p.getGender()) + "</td>");

                        out.println("<td>");

                        // EDIT button
                        out.println("<form class='inline' action='" + cp + "/edit' method='get'>");
                        out.println("<input type='hidden' name='pesel' value='" + html(p.getPesel()) + "'>");
                        out.println("<button class='btn btn-blue' type='submit'>✏️ Edit</button>");
                        out.println("</form>");

                        // DELETE button
                        out.println("<form class='inline' action='" + cp + "/compute' method='post' style='margin-left:8px;'>");
                        out.println("<input type='hidden' name='action' value='delete'>");
                        out.println("<input type='hidden' name='pesel' value='" + html(p.getPesel()) + "'>");
                        out.println("<button class='btn btn-red' type='submit'>🗑️ Delete</button>");
                        out.println("</form>");

                        out.println("</td>");
                        out.println("</tr>");
                    }
                    out.println("</table>");
                }

                // DB OPERATIONS
                out.println("<h2>🧾 Database: Operation Log (" + ops.size() + ")</h2>");
                if (ops.isEmpty()) {
                    out.println("<p>No operations in database.</p>");
                } else {
                    out.println("<table>");
                    out.println("<tr><th>#</th><th>Time</th><th>Type</th><th>Patient PESEL</th><th>Detail</th></tr>");

                    int j = 1;
                    for (OperationLogEntity o : ops) {
                        String peselOfOp = "(none)";
                        try {
                            if (o.getPatient() != null) {
                                peselOfOp = o.getPatient().getPesel();
                            }
                        } catch (Exception ignored) {
                            // in case LAZY fetch causes problems, keep "(none)"
                        }

                        out.println("<tr>");
                        out.println("<td>" + (j++) + "</td>");
                        out.println("<td>" + html(String.valueOf(o.getOperationTime())) + "</td>");
                        out.println("<td>" + html(String.valueOf(o.getOperationType())) + "</td>");
                        out.println("<td>" + html(peselOfOp) + "</td>");
                        out.println("<td>" + html(o.getDetail()) + "</td>");
                        out.println("</tr>");
                    }
                    out.println("</table>");
                }

                out.println("</body></html>");
            }

        } catch (PersistenceException ex) {
            response.sendRedirect(cp + "/form?error=db&detail=" + url(safeMsg(ex)));
        } catch (Exception ex) {
            response.sendRedirect(cp + "/form?error=server_error&detail=" + url(safeMsg(ex)));
        }
    }

    private String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String url(String s) {
        if (s == null) s = "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private String safeMsg(Exception ex) {
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? ex.getClass().getSimpleName() : m;
    }

    private String decode(String s) {
        if (s == null) return null;
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private String noneIfBlank(String s) {
        if (s == null || s.isBlank()) return "(none)";
        return s;
    }
}