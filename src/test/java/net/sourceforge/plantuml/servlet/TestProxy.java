package net.sourceforge.plantuml.servlet;

import com.meterware.httpunit.Base64;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class TestProxy extends WebappTestCase {
    /**
     * Verifies the proxified reception of the default Bob and Alice diagram
     */
    public void testDefaultProxy() throws Exception {
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getServerUrl()+"proxy/png/https://github.scm.corp.ebay.com/MobilePlatform/AccessControlService/raw/master/docs/add_user_role.puml");
//        WebRequest request = new GetMethodWebRequest(getServerUrl()+"proxy/"+getServerUrl()+"/welcome");
        WebResponse response = conversation.getResource(request);
        // Analyze response
        // Verifies the Content-Type header
        // assertEquals( "Response content type is not PNG", "image/png", response.getContentType());
        // Get the image and verify its size (~2000 bytes)
        InputStream responseStream = response.getInputStream();
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while ((n = responseStream.read(buf)) != -1) {
            imageStream.write(buf, 0, n);
        }
        imageStream.close();
        responseStream.close();
        byte[] inMemoryImage = imageStream.toByteArray();
        int diagramLen = inMemoryImage.length;
        assertTrue(diagramLen > 100);
        assertTrue(diagramLen < 25000);
    }

    public void testProxyWithFormat() throws Exception {
        WebConversation conversation = new WebConversation();
        WebRequest request = new GetMethodWebRequest(getServerUrl() + "proxy/svg/" + getServerUrl() + "welcome");
        WebResponse response = conversation.getResource(request);
        // Analyze response
        // Verifies the Content-Type header
        // TODO assertEquals( "Response content type is not SVG", "image/svg+xml", response.getContentType());
        // Get the content and verify its size
        String diagram = response.getText();
        int diagramLen = diagram.length();
        assertTrue(diagramLen > 1000);
        assertTrue(diagramLen < 3000);
    }

    /**
     * Verifies that the HTTP header of a diagram incites the browser to cache it.
     */
    public void testInvalidUrl() throws Exception {
        WebConversation conversation = new WebConversation();
        // Try to proxify an invalid address
        WebRequest request = new GetMethodWebRequest(getServerUrl() + "proxy/invalidURL");
        WebResponse response = conversation.getResource(request);
        // Analyze response, it must be the empty form
        // Verifies the Content-Type header
        assertEquals("Response content type is not HTML", "text/html", response.getContentType());
        WebForm forms[] = response.getForms();
        assertEquals(2, forms.length);
    }

    public void testUrl() throws Exception {
        String line;
        OutputStreamWriter wr = null;
        BufferedReader rd  = null;
        StringBuilder sb = null;
        String https_url = "https://github.scm.corp.ebay.com/MobilePlatform/AccessControlService/raw/master/docs/add_user_role.puml";
        URL url;
        try {

            url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            con.setRequestProperty("Authorization", "Basic "+Base64.encode("erchan:TinyCouch!6"));
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

            System.out.println(sb.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            rd = null;
            wr = null;
        }
    }
}
