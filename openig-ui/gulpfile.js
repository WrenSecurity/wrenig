/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2023 Wren Security.
 */
const {
    useBuildScripts,
    useEslint,
    useLocalResources,
    useModuleResources,
    useLessStyles,
    useBuildRequire
} = require("@wrensecurity/commons-ui-build");
const gulp = require("gulp");
const { runQunitPuppeteer, printResultSummary } = require("node-qunit-puppeteer");
const { join } = require("path");
const { pathToFileURL } = require("url");

const TARGET_PATH = "target/www";
const TESTS_PATH = "target/test";

const MODULE_RESOURCES = {
    "d3/d3.min.js": "libs/d3.js",
    "dragula/dist/dragula.min.css": "css/dragula.css",
    "dragula/dist/dragula.min.js": "libs/dragula.js",
    "qunit/qunit/qunit.css": "css/qunit.css",
    "qunit/qunit/qunit.js": "libs/qunit.js",
    //~ Code mirror resources
    "codemirror/lib/codemirror.css": "css/codemirror/codemirror.css",
    "codemirror/lib/codemirror.js": "libs/codemirror/lib/codemirror.js",
    "codemirror/mode/javascript/javascript.js": "libs/codemirror/mode/javascript/javascript.js"
};

const LOCAL_RESOURCES = {
    "js/*": "libs"
};

const TEST_RESOURCES = {
    "src/test/**": ""
};

gulp.task("eslint", useEslint({ src: "src/main/js/**/*.js" }));

gulp.task("build:assets", useLocalResources({ "src/main/resources/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:scripts", useBuildScripts({
    src: "src/main/js/**/*.js",
    dest: TARGET_PATH
}));

gulp.task("build:compose", useLocalResources({ "target/ui-compose/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:libs", async () => {
    await useModuleResources(MODULE_RESOURCES, { path: __filename, dest: TARGET_PATH })();
    await useLocalResources(LOCAL_RESOURCES, { base: "libs", dest: TARGET_PATH })();
});

gulp.task("build:styles", useLessStyles({
    "target/www/css/structure.less": "css/structure.css",
    "target/www/css/theme.less": "css/theme.css"
}, { base: join(TARGET_PATH, "css"), dest: TARGET_PATH }));

gulp.task("build:bundle", useBuildRequire({
    base: TARGET_PATH,
    src: "src/main/js/main.js",
    dest: join(TARGET_PATH, "main.js"),
    exclude: [
        // Excluded from optimization so that the UI can be customized without having to repackage it.
        "config/AppConfiguration",
        // Exclude mock project dependencies to create a more representative bundle.
        "mock/Data",
        "sinon"
    ]
}));

gulp.task("test:scripts", useLocalResources(TEST_RESOURCES, { dest: TESTS_PATH }));

gulp.task("test:sinon", useBuildScripts({
    src: require.resolve("sinon/pkg/sinon.js"),
    dest: join(TARGET_PATH, "libs")
}));

gulp.task("test:qunit", async () => {
    const result = await runQunitPuppeteer({
        targetUrl: pathToFileURL(join(TESTS_PATH, "index.html")).href,
        puppeteerArgs: [
            "--allow-file-access-from-files",
            "--no-sandbox"
        ]
    });
    printResultSummary(result, console);
});

gulp.task("build", gulp.series(
    gulp.parallel(
        "build:assets",
        "build:scripts",
        "build:compose",
        "build:libs",
        "test:sinon"
    ),
    gulp.parallel(
        "build:styles",
        "build:bundle"
    )
));

// TODO FIXME QUnit tests are temporarily disabled
gulp.task("test", gulp.series(
    "test:scripts",
    "test:qunit"
));

gulp.task("watch", () => {
    gulp.watch("src/main/js/**", gulp.parallel("build:scripts"));
});

gulp.task("default", gulp.series("eslint", "build", "test"));
