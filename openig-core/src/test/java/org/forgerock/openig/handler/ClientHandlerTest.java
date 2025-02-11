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
 * Portions Copyright 2023-2025 Wren Security.
 */

package org.forgerock.openig.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.util.Options.defaultOptions;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.forgerock.http.Handler;
import org.forgerock.http.handler.HttpClientHandler;
import org.forgerock.http.protocol.Request;
import org.forgerock.http.protocol.Response;
import org.forgerock.http.protocol.Status;
import org.forgerock.services.context.RootContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class ClientHandlerTest {

    private WireMockServer server;

    @Mock
    private Handler delegate;

    @BeforeClass
    public void init() throws Exception {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();
    }

    @AfterClass
    public void teardown() throws Exception {
        server.stop();
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        MockitoAnnotations.openMocks(this);
        server.resetAll();
    }

    @Test(description = "test for OPENIG-315")
    public void checkRequestIsForwardedUnaltered() throws Exception {
        server.stubFor(post("/example").willReturn(ok()));
        try (HttpClientHandler clientHandler = new HttpClientHandler(defaultOptions())) {
            Request request = new Request();
            request.setMethod("POST");
            request.setUri("http://0.0.0.0:" + server.port() + "/example");

            final Map<String, Object> json = new LinkedHashMap<>();
            json.put("k1", "v1");
            json.put("k2", "v2");
            request.setEntity(json);

            ClientHandler handler = new ClientHandler(clientHandler);
            Response response = handler.handle(null, request).get();

            assertThat(response.getStatus()).isEqualTo(Status.OK);
            server.verify(postRequestedFor(urlEqualTo("/example")).withRequestBody(equalTo("{\"k1\":\"v1\",\"k2\":\"v2\"}")));
        }
    }

    @Test
    public void shouldSendPostHttpMessageWithEntityContent() throws Exception {
        server.stubFor(post("/test").willReturn(ok("Hello")));
        try (HttpClientHandler clientHandler = new HttpClientHandler(defaultOptions())) {
            ClientHandler handler = new ClientHandler(clientHandler);
            Request request = new Request();
            request.setMethod("POST");
            request.setUri(format("http://localhost:%d/test", server.port()));
            request.getEntity().setString("Hello");
            assertThat(handler.handle(new RootContext(), request).get().getStatus())
                    .isEqualTo(Status.OK);
        }
    }

    @Test
    public void shouldSendPostHttpMessageWithEmptyEntity() throws Exception {
        server.stubFor(post("/test").willReturn(ok()));
        try (HttpClientHandler clientHandler = new HttpClientHandler(defaultOptions())) {
            ClientHandler handler = new ClientHandler(clientHandler);
            Request request = new Request();
            request.setMethod("POST");
            request.setUri(format("http://localhost:%d/test", server.port()));
            assertThat(handler.handle(new RootContext(), request).get().getStatus()).isEqualTo(Status.OK);
        }
    }
}
