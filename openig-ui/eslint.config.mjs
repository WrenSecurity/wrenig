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
 * Copyright 2025-2026 Wren Security.
 */

import globals from "globals";
import wrensecurityConfig from "@wrensecurity/eslint-config";

export default [
    wrensecurityConfig,
    {
        languageOptions: {
            globals: {
                ...globals.browser,
                ...globals.amd,
                ...globals.qunit,
                ...globals.es2015
            }
        },
        rules: {
            "arrow-parens": ["error", "always"],
            "arrow-spacing": "error",
            "indent": ["error", 4, {
                "FunctionDeclaration": {
                    "parameters": 2
                },
                "FunctionExpression": {
                    "parameters": 1
                },
                "SwitchCase": 1,
                "VariableDeclarator": 1
            }],
            "no-alert": "error",
            "no-extend-native": "error",
            "no-global-assign": "error",
            "no-mixed-spaces-and-tabs": "error",
            "no-multiple-empty-lines": "error",
            "no-multi-str": "error",
            "no-shadow": "error",
            "no-unused-vars": "error",
            "no-void": "error"
        }
    },
    {
        files: ["gulpfile.js"],
        languageOptions: {
            ecmaVersion: 2021,
            globals: {
                ...globals.node
            }
        }
    },
    {
        files: ["**/*.jsm"],
        languageOptions: {
            sourceType: "module",
            ecmaVersion: 2021
        }
    },
    {
        files: ["**/*.mjs"],
        languageOptions: {
            sourceType: "module",
            ecmaVersion: 2021,
            globals: {
                ...globals.node
            }
        }
    }
];
