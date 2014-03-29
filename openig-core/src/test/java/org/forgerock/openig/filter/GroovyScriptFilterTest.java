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
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */
package org.forgerock.openig.filter;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.builder.verify.VerifyHttp.verifyHttp;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;

import javax.script.ScriptException;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.opendj.ldap.Connections;
import org.forgerock.opendj.ldap.LDAPClientContext;
import org.forgerock.opendj.ldap.LDAPListener;
import org.forgerock.opendj.ldap.MemoryBackend;
import org.forgerock.opendj.ldif.LDIFEntryReader;
import org.forgerock.openig.handler.Handler;
import org.forgerock.openig.handler.HandlerException;
import org.forgerock.openig.heap.HeapImpl;
import org.forgerock.openig.http.Exchange;
import org.forgerock.openig.http.HttpClient;
import org.forgerock.openig.http.Request;
import org.forgerock.openig.http.Response;
import org.forgerock.openig.http.Session;
import org.forgerock.openig.io.ByteArrayBranchingStream;
import org.forgerock.openig.io.TemporaryStorage;
import org.forgerock.openig.log.LogTimer;
import org.forgerock.openig.log.Logger;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.testng.annotations.Test;

import com.xebialabs.restito.server.StubServer;

/**
 * Tests the Groovy scripting filter.
 */
@SuppressWarnings("javadoc")
public class GroovyScriptFilterTest {

    private static final String XML_CONTENT =
            "<root><a a1='one'><b>3 &lt; 5</b><c a2='two'>blah</c></a></root>";
    private static final String JSON_CONTENT = "{\"person\":{" + "\"firstName\":\"Tim\","
            + "\"lastName\":\"Yates\"," + "\"address\":{\"city\":\"Manchester\","
            + "\"country\":\"UK\",\"zip\":\"M1 2AB\"}}}";

    @Test
    public void testNextHandlerCanBeInvoked() throws Exception {
        final GroovyScriptFilter filter = new GroovyScriptFilter("next.handle(exchange)");
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        verify(handler).handle(exchange);
    }

    @Test
    public void testNextHandlerCanThrowHandlerException() throws Exception {
        final GroovyScriptFilter filter = new GroovyScriptFilter("next.handle(exchange)");
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        final HandlerException expected = new HandlerException();
        doThrow(expected).when(handler).handle(exchange);
        try {
            filter.filter(exchange, handler);
            fail();
        } catch (final HandlerException e) {
            assertThat(e).isSameAs(expected);
        }
    }

    @Test
    public void testNextHandlerPreAndPostConditions() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.response == null",
                "next.handle(exchange)",
                "assert exchange.response != null");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Response expectedResponse = new Response();
        final Handler handler = mock(Handler.class);
        returnResponse(expectedResponse).when(handler).handle(exchange);
        filter.filter(exchange, handler);
        verify(handler).handle(exchange);
        assertThat(exchange.response).isSameAs(expectedResponse);
    }

    private Stubber returnResponse(final Response response) {
        return doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) {
                final Object[] args = invocation.getArguments();
                ((Exchange) args[0]).response = response;
                return null;
            }
        });
    }

    @Test
    public void testBindingsArePresent() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                        "assert exchange != null",
                        "assert exchange.request != null",
                        "assert exchange.response == null",
                        "assert logger != null",
                        "assert next != null");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        verifyZeroInteractions(handler);
    }

    @Test
    public void testConstructFromFile() throws Exception {
        final HeapImpl heap = new HeapImpl();
        heap.put("TemporaryStorage", new TemporaryStorage());
        final GroovyScriptFilter filter =
                (GroovyScriptFilter) new GroovyScriptFilter.Heaplet().create("test", new JsonValue(
                        Collections.singletonMap("scriptFile", "src/test/resources/test.groovy")),
                        heap);
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        verifyZeroInteractions(handler);
        assertThat(exchange.response).isNotNull();
    }

    @Test
    public void testConstructFromString() throws Exception {
        final HeapImpl heap = new HeapImpl();
        heap.put("TemporaryStorage", new TemporaryStorage());
        final String script =
                "import org.forgerock.openig.http.Response;exchange.response = new Response()";
        final GroovyScriptFilter filter =
                (GroovyScriptFilter) new GroovyScriptFilter.Heaplet().create("test", new JsonValue(
                        Collections.singletonMap("script", script)), heap);
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        verifyZeroInteractions(handler);
        assertThat(exchange.response).isNotNull();
    }

    @Test
    public void testThrowHandlerException() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "import org.forgerock.openig.handler.HandlerException",
                "throw new HandlerException(\"test\")");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        try {
            filter.filter(exchange, handler);
            fail();
        } catch (final HandlerException e) {
            assertThat(e.getMessage()).isEqualTo("test");
        }
    }

    @Test
    public void testSetResponse() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "import org.forgerock.openig.http.Response",
                "exchange.response = new Response()",
                "exchange.response.status = 404");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        assertThat(exchange.response).isNotNull();
        assertThat(exchange.response.status).isEqualTo(404);
    }

    @Test
    public void testLogging() throws Exception {
        final GroovyScriptFilter filter = new GroovyScriptFilter("logger.error('test')");
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.logger = mock(Logger.class);
        when(filter.logger.getTimer()).thenReturn(new LogTimer(filter.logger));
        filter.filter(exchange, handler);
        verify(filter.logger).error("test");
    }

    @Test(expectedExceptions = ScriptException.class)
    public void testCompilationFailure() throws Exception {
        new GroovyScriptFilter("import does.not.Exist");
    }

    @Test(expectedExceptions = ScriptException.class)
    public void testRunTimeFailure() throws Throwable {
        final GroovyScriptFilter filter = new GroovyScriptFilter("dummy + 1");
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        try {
            filter.filter(exchange, handler);
            fail();
        } catch (final HandlerException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testAssignment() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "exchange.test = false",
                "next.handle(exchange)",
                "exchange.test = exchange.response.status == 302");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        Response response = new Response();
        response.status = 302;
        returnResponse(response).when(handler).handle(exchange);
        filter.filter(exchange, handler);
        assertThat(exchange.get("test")).isEqualTo(true);
    }

    @Test
    public void testRequestForm() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.request.form.username[0] == 'test'");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.uri = new URI("http://test?username=test");
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
    }

    @Test
    public void testRequestCookies() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.request.cookies.username[0].value == 'test'");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.headers.add("Cookie", "username=test;Path=/");
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
    }

    @Test
    public void testRequestURI() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.request.uri.scheme == 'http'",
                "assert exchange.request.uri.host == 'example.com'",
                "assert exchange.request.uri.port == 8080",
                "assert exchange.request.uri.path == '/users'",
                "assert exchange.request.uri.query == 'action=create'");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.uri = new URI("http://example.com:8080/users?action=create");
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
    }

    @Test
    public void testRequestHeaders() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.request.headers.Username[0] == 'test'",
                "exchange.request.headers.Test = [ 'test' ]",
                "assert exchange.request.headers.remove('Username')");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.headers.add("Username", "test");
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        assertThat(exchange.request.headers.get("Test")).containsOnly("test");
        assertThat(exchange.request.headers.get("Username")).isNull();
    }

    @Test
    public void testSession() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.session.inKey == 'inValue'",
                "exchange.session.outKey = 'outValue'",
                "assert exchange.session.remove('inKey')");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.session = mock(Session.class);
        when(exchange.session.get("inKey")).thenReturn("inValue");
        when(exchange.session.remove("inKey")).thenReturn(true);
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        verify(exchange.session).get("inKey");
        verify(exchange.session).put("outKey", "outValue");
        verify(exchange.session).remove("inKey");
        verifyNoMoreInteractions(exchange.session);
    }

    @Test
    public void testGlobalsPersistedBetweenInvocations() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert globals.x == null",
                "globals.x = 'value'");
        // @formatter:on
        final Exchange exchange = new Exchange();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        try {
            filter.filter(exchange, handler);
            fail("Second iteration succeeded unexpectedly");
        } catch (AssertionError e) {
            // Expected.
        }
    }

    @Test
    public void testHttpClient() throws Exception {
        // Create mock HTTP server.
        final StubServer server = new StubServer().run();
        whenHttp(server).match(get("/example")).then(status(HttpStatus.OK_200),
                stringContent(JSON_CONTENT));
        try {
            final int port = server.getPort();
            // @formatter:off
            final GroovyScriptFilter filter = new GroovyScriptFilter(
                    "import org.forgerock.openig.http.*",
                    "Request request = new Request()",
                    "request.method = 'GET'",
                    "request.uri = new URI('http://0.0.0.0:" + port + "/example')",
                    "exchange.response = http.execute(request)");
            filter.setHttpClient(new HttpClient(new TemporaryStorage()));

            // @formatter:on
            final Exchange exchange = new Exchange();
            exchange.request = new Request();
            final Handler handler = mock(Handler.class);
            filter.filter(exchange, handler);

            verifyHttp(server).once(method(Method.GET), uri("/example"));
            assertThat(exchange.response.status).isEqualTo(200);
            assertThat(s(exchange.response.entity)).isEqualTo(JSON_CONTENT);
        } finally {
            server.stop();
        }
    }

    @Test
    public void testLdapClient() throws Exception {
        // Create mock LDAP server with a single user.
        // @formatter:off
        final MemoryBackend backend = new MemoryBackend(new LDIFEntryReader(
                "dn:",
                "objectClass: top",
                "objectClass: extensibleObject",
                "",
                "dn: dc=com",
                "objectClass: domain",
                "objectClass: top",
                "dc: com",
                "",
                "dn: dc=example,dc=com",
                "objectClass: domain",
                "objectClass: top",
                "dc: example",
                "",
                "dn: ou=people,dc=example,dc=com",
                "objectClass: organizationalUnit",
                "objectClass: top",
                "ou: people",
                "",
                "dn: uid=bjensen,ou=people,dc=example,dc=com",
                "objectClass: top",
                "objectClass: person",
                "objectClass: organizationalPerson",
                "objectClass: inetOrgPerson",
                "cn: Barbara",
                "sn: Jensen",
                "uid: bjensen",
                "description: test user",
                "userPassword: password"));
        // @formatter:on
        final LDAPListener listener =
                new LDAPListener(0, Connections
                        .<LDAPClientContext> newServerConnectionFactory(backend));
        final int port = listener.getPort();
        try {
            // @formatter:off
            final GroovyScriptFilter filter = new GroovyScriptFilter(
                    "import org.forgerock.opendj.ldap.*",
                    "import org.forgerock.openig.http.Response",
                    "",
                    "username = exchange.request.headers.Username[0]",
                    "password = exchange.request.headers.Password[0]",
                    "",
                    "exchange.response = new Response()",
                    "",
                    "client = ldap.connect('0.0.0.0'," + port + ")",
                    "try {",
                    "  user = client.searchSingleEntry('ou=people,dc=example,dc=com',",
                    "                                  ldap.scope.sub,",
                    "                                  ldap.filter('uid=%s', username))",
                    "  client.bind(user.name.toString(), password.toCharArray())",
                    "  exchange.response.status = 200",
                    // Attributes as MetaClass properties
                    "  exchange.response.reason = user.description.parse().asString()",
                    "  user.description = 'some value'",
                    "  assert user.description.parse().asString() == 'some value'",
                    "} catch (AuthenticationException e) {",
                    "  exchange.response.status = 403",
                    "  exchange.response.reason = e.message",
                    "} catch (Exception e) {",
                    "  exchange.response.status = 500",
                    "  exchange.response.reason = e.message",
                    "} finally {",
                    "  client.close()",
                    "}");
            // @formatter:on

            // Authenticate using correct password.
            final Exchange exchange = new Exchange();
            exchange.request = new Request();
            exchange.request.headers.add("Username", "bjensen");
            exchange.request.headers.add("Password", "password");
            final Handler handler = mock(Handler.class);
            filter.filter(exchange, handler);
            assertThat(exchange.response.status).as(exchange.response.reason).isEqualTo(200);
            assertThat(exchange.response.reason).isEqualTo("test user");

            // Authenticate using wrong password.
            exchange.request = new Request();
            exchange.request.headers.add("Username", "bjensen");
            exchange.request.headers.add("Password", "wrong");
            filter.filter(exchange, handler);
            assertThat(exchange.response.status).isEqualTo(403);
            assertThat(exchange.response.reason).isNotNull();
        } finally {
            listener.close();
        }
    }

    @Test(enabled = false)
    public void testWriteJsonEntity() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "exchange.request.jsonOut.person {",
                    "firstName 'Tim'",
                    "lastName 'Yates'",
                    "address {",
                        "city: 'Manchester'",
                        "country: 'UK'",
                        "zip: 'M1 2AB'",
                    "}",
                "}");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        assertThat(s(exchange.request.entity)).isEqualTo(JSON_CONTENT);
    }

    @Test(enabled = false)
    public void testReadJsonEntity() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.request.jsonIn.person.firstName == 'Tim'",
                "assert exchange.request.jsonIn.person.lastName == 'Yates'",
                "assert exchange.request.jsonIn.person.address.country == 'UK'");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.entity = new ByteArrayBranchingStream(JSON_CONTENT.getBytes("UTF-8"));
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
    }

    @Test(enabled = false)
    public void testWriteXmlEntity() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "exchange.request.xmlOut.root {",
                    "a( a1:'one' ) {",
                        "b { mkp.yield( '3 < 5' ) }",
                        "c( a2:'two', 'blah' )",
                    "}",
                "}");
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
        assertThat(s(exchange.request.entity)).isEqualTo(XML_CONTENT);
    }

    @Test(enabled = false)
    public void testReadXmlEntity() throws Exception {
        // @formatter:off
        final GroovyScriptFilter filter = new GroovyScriptFilter(
                "assert exchange.request.xmlIn.root.a"); // TODO
        // @formatter:on
        final Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.entity = new ByteArrayBranchingStream(XML_CONTENT.getBytes("UTF-8"));
        final Handler handler = mock(Handler.class);
        filter.filter(exchange, handler);
    }

    private String s(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }
}