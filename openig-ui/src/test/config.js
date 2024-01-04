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
 * Portions Copyright 2024 Wren Security.
 */

require.config({
    baseUrl: "../www",
    map: {
        "*": {
            "ThemeManager": "org/forgerock/openig/ui/common/util/ThemeManager"
        }
    },
    paths: {
        sinon: "libs/sinon",
        i18next: "libs/i18next",
        backbone: "libs/backbone",
        "backbone.paginator": "libs/backbone.paginator",
        "backbone-relational": "libs/backbone-relational",
        "backgrid": "libs/backgrid",
        "backgrid-filter": "libs/backgrid-filter",
        "backgrid-paginator": "libs/backgrid-paginator",
        selectize: "libs/selectize",
        lodash: "libs/lodash",
        js2form: "libs/js2form",
        form2js: "libs/form2js",
        spin: "libs/spin",
        jquery: "libs/jquery",
        xdate: "libs/xdate",
        doTimeout: "libs/jquery.ba-dotimeout",
        handlebars: "libs/handlebars",
        moment: "libs/moment",
        bootstrap: "libs/bootstrap",
        "bootstrap-dialog": "libs/bootstrap-dialog",
        placeholder: "libs/jquery.placeholder",
        dragula: "libs/dragula",
        d3: "libs/d3",
        dimple: "libs/dimple",
        underscore : "libs/underscore"
    },
    shim: {
        sinon: {
            exports: "sinon"
        },
        lodash: {
            exports: "_"
        },
        backbone: {
            deps: ["lodash"],
            exports: "Backbone"
        },
        "backbone.paginator": {
            deps: ["backbone"]
        },
        "backgrid": {
            deps: ["jquery", "lodash", "backbone"],
            exports: "Backgrid"
        },
        "backgrid-filter": {
            deps: ["backgrid"]
        },
        "backgrid-paginator": {
            deps: ["backgrid", "backbone.paginator"]
        },
        js2form: {
            exports: "js2form"
        },
        form2js: {
            exports: "form2js"
        },
        spin: {
            exports: "spin"
        },
        bootstrap: {
            deps: ["jquery"]
        },
        "bootstrap-dialog": {
            deps: ["jquery", "lodash", "backbone", "bootstrap"]
        },
        placeholder: {
            deps: ["jquery"]
        },
        selectize: {
            deps: ["jquery"]
        },
        xdate: {
            exports: "xdate"
        },
        doTimeout: {
            deps: ["jquery"],
            exports: "doTimeout"
        },
        i18next: {
            deps: ["jquery", "handlebars"],
            exports: "i18n"
        },
        moment: {
            exports: "moment"
        },
        d3: {
            exports: "d3"
        },
        dimple: {
            exports: "dimple",
            deps: ["d3"]
        }
    }
});

require([
    "org/forgerock/openig/ui/common/main/MockServer",
    "jquery",
    "bootstrap"
], (MockServer, $) => {

    $("head", document).append("<base href='../www/' />");

    require(["main", "../test/run"], (appMain, run) => {
        run(MockServer.instance);
    });

});
