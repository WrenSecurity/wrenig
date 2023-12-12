/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2016 ForgeRock AS.
 * Portions Copyright 2023 Wren Security.
 */

package org.forgerock.openig.doc;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class SampleApplicationTest {

    private static final Logger logger = Logger.getLogger(SampleApplicationTest.class.getName());
    public static final String HOME_TITLE = "Sample application home page";
    private static final String LOGIN_TITLE = "Howdy, Anonymous User";
    private static final String PROFILE_TITLE = "Howdy, demo";

    private static HttpServer httpServer;
    private HttpClient httpClient;
    private String port;
    private String sslPort;

    private String httpServerPath;
    private String httpsServerPath;

    @BeforeTest
    public void setUp() throws Exception {
        port = System.getProperty("serverPort");
        sslPort = System.getProperty("serverSslPort");
        logger.info("Port: " + port + ", SSL Port: " + sslPort);
        httpServer = SampleApplication.start(Integer.parseInt(port), Integer.parseInt(sslPort));
        httpServerPath = "http://localhost:" + port;
        httpsServerPath = "https://localhost:" + sslPort;
        // Prepare HTTP client ignoring SSL server certificate verification
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { new TrustAllCertsManager() }, new SecureRandom());
        httpClient = HttpClient.newBuilder().sslContext(sslContext).build();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        logger.info("Shutting down server");
        SampleApplication.stop(httpServer);
    }

    @Test
    public void testGetHomePage() throws Exception {
        logger.info("Testing equivalent of curl --verbose " + httpServerPath + "/home");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(httpServerPath + "/home"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains(HOME_TITLE));
    }

    @Test
    public void testGetLoginPage() throws Exception {
        // Check for HTTP 200 OK and the Login page in the body of the response
        logger.info("Testing equivalent of curl --verbose " + httpServerPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(httpServerPath))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains(LOGIN_TITLE));
    }

    @Test
    public void testGetLoginPageHttps() throws Exception {
        // Given
        logger.info("Testing the equivalent of curl --verbose " + httpsServerPath);

        // When
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(httpsServerPath))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Then
        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains(LOGIN_TITLE));
    }

    @Test
    public void testPostValidCredentials() throws Exception {

        // Check for HTTP 200 OK and the username in the body of the response
        logger.info("Testing equivalent of "
                + "curl --verbose --data \"username=demo&password=changeit\" " + httpServerPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(httpsServerPath))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=demo&password=changeit"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains("Howdy, demo"));
    }

    @Test
    public void testPostValidCredentialsAsHeaders() throws Exception {
        logger.info("Testing equivalent of "
                + "curl --verbose --H \"username: demo\" --H \"password=changeit\" " + httpServerPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(httpsServerPath))
                .headers("username", "demo")
                .headers("password", "changeit")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 200);
        assertTrue(response.body().contains(PROFILE_TITLE));
    }

    @Test
    public void postIncompleteCredentials() throws Exception {

        // Check for HTTP 401 Authorization Required
        logger.info("Testing equivalent of "
                + "curl --verbose --data \"username=no-password\" " + httpServerPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(httpsServerPath))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=no-password"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 401);
    }

    @Test
    public void postInvalidCredentials() throws Exception {

        // Check for HTTP 403 Forbidden
        logger.info("Testing equivalent of "
                + "curl --verbose --data \"username=wrong&password=wrong\" " + httpServerPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(httpsServerPath))
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("username=wrong&password=wrong"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 403);
    }

    @Test
    public void testValidWebFingerRequest() throws Exception {
        logger.info("Testing equivalent of curl " + httpServerPath
                + "/.well-known/webfinger?resource=resource");
        final String webFingerUrl = httpServerPath + "/.well-known/webfinger?resource=resource";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webFingerUrl))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 200);
        assertEquals(response.body(),
                "{ \"subject\": \"resource\", \"links\": "
                        + "[ { \"rel\": \"http://openid.net/specs/connect/1.0/issuer\", "
                        + "\"href\": \"http://openam.example.com:8088/openam/oauth2\" } ] }");
    }

    @Test
    public void testBrokenWebFingerRequest() throws Exception {
        logger.info("Testing equivalent of curl " + httpServerPath + "/.well-known/webfinger");

        final String webFingerUrl = httpServerPath + "/.well-known/webfinger";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webFingerUrl))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), 400);
        assertEquals(response.body(),
                "{ \"error\": \"Request must include a resource parameter.\" }");
    }

    /**
     * Trust manager accepting all certificates.
     */
    private class TrustAllCertsManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }
}
