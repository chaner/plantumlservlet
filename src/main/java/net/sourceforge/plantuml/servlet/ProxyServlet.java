/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * Project Info:  http://plantuml.sourceforge.net
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 */
package net.sourceforge.plantuml.servlet;

import HTTPClient.CookieModule;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 
 * Proxy servlet of the webapp.
 * This servlet retrieves the diagram source of a web resource (web html page)
 * and renders it.
 */
@SuppressWarnings("serial")
public class ProxyServlet extends HttpServlet {

    private static final Pattern proxyPattern = Pattern.compile("/\\w+/proxy/((\\d+)/)?((\\w+)/)?(https://.*)");
    private String format;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        final String uri = request.getRequestURI();

        Matcher proxyMatcher = proxyPattern.matcher(uri);
        if (proxyMatcher.matches()) {
            String num = proxyMatcher.group(2); // Optional number of the diagram source
            format = proxyMatcher.group(4); // Expected format of the generated diagram
            String sourceURL = proxyMatcher.group(5);
            handleImageProxy(response, num, sourceURL);
        } else {
            request.setAttribute("net.sourceforge.plantuml.servlet.decoded", "ERROR Invalid proxy syntax : " + uri);
            request.removeAttribute("net.sourceforge.plantuml.servlet.encoded");

            // forward to index.jsp
            RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
        }
    }

    private void handleImageProxy(HttpServletResponse response, String num, String source) throws IOException {
        SourceStringReader reader = new SourceStringReader(getSource(source));
        int n = num == null ? 0 : Integer.parseInt(num);

        reader.generateImage(response.getOutputStream(), n, new FileFormatOption(getOutputFormat(), false));
    }

    private String getSource(String uri) throws IOException {
        CookieModule.setCookiePolicyHandler(null);

        final Pattern p = Pattern.compile("https://[^/]+(/?.*)");
        final Matcher m = p.matcher(uri);
        if (m.find() == false) {
            throw new IOException(uri);
        }
        final URL url = new URL(uri);

        String line;
        OutputStreamWriter wr = null;
        BufferedReader rd  = null;
        StringBuilder sb = null;
        try {

            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setReadTimeout(10000);

            con.connect();
            rd  = new BufferedReader(new InputStreamReader(con.getInputStream()));
            sb = new StringBuilder();

            while ((line = rd.readLine()) != null)
            {
                sb.append(line + '\n');
            }

            return sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            rd = null;
            wr = null;
        }
        return "";
    }

    private FileFormat getOutputFormat() {
        if (format == null) {
            return FileFormat.PNG;
        }
        if (format.equals("svg")) {
            return FileFormat.SVG;
        }
        if (format.equals("txt")) {
            return FileFormat.ATXT;
        }
        return FileFormat.PNG;
    }

}
