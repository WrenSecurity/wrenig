/**
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
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2023-2024 Wren Security.
 */

/* global Backbone, _ */

define([
    "jquery",
    "org/forgerock/commons/ui/common/util/Constants",
    "org/forgerock/commons/ui/common/main/EventManager",
    "./tests/OpenIGValidatorsTests",
    "./tests/TransformServiceTests",
    "./tests/DataFilterTests",
    "./tests/getLoggedUser"
], (
    $,
    constants,
    eventManager,
    openIGValidatorsTests,
    transformServiceTests,
    dataFilterTests,
    getLoggedUser) => {

    $.doTimeout = function (name, time, func) {
        func(); // run the function immediately rather than delayed.
    };

    return function (server) {

        eventManager.registerListener(constants.EVENT_APP_INITIALIZED, () => {
            require("ThemeManager").getTheme().then(() => {
                QUnit.testStart((testDetails) => {
                    console.log(`Starting ${testDetails.module}":"${testDetails.name}(${testDetails.testNumber})`);

                    require("org/forgerock/commons/ui/common/main/Configuration").baseTemplate = null;
                });


                _.delay(() => {
                    QUnit.start();
                    openIGValidatorsTests.executeAll(server, getLoggedUser());
                    transformServiceTests.executeAll(server);
                    dataFilterTests.executeAll(server);
                }, 500);

                QUnit.done(() => {
                    localStorage.clear();
                    Backbone.history.stop();
                    window.location.hash = "";
                });
            });
        });
    };

});
