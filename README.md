<!--
  The contents of this file are subject to the terms of the Common Development and
  Distribution License (the License). You may not use this file except in compliance with the
  License.

  You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
  specific language governing permission and limitations under the License.

  When distributing Covered Software, include this CDDL Header Notice in each file and include
  the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
  Header, with the fields enclosed by brackets [] replaced by your own identifying
  information: "Portions copyright [year] [name of copyright owner]".

  Copyright 2015-2016 ForgeRock AS.
  Portions Copyright 2024 Wren Security.
  -->

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://github.com/WrenSecurity/wrenig/assets/13997406/e6ded2ad-3ce6-4745-907a-7eaa33d93df9">
    <source media="(prefers-color-scheme: light)" srcset="https://github.com/WrenSecurity/wrenig/assets/13997406/29b9b12e-26df-4057-bcce-ff6fd11c4397">
    <img alt="Wren:IG logo" src="https://github.com/WrenSecurity/wrenig/assets/13997406/29b9b12e-26df-4057-bcce-ff6fd11c4397" width="60%">
  </picture>
</p>

# Wren:IG

[![License](https://img.shields.io/badge/license-CDDL-blue.svg)](https://github.com/WrenSecurity/wrenig/blob/main/LICENSE)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://app.element.io/#/room/#WrenSecurity_Lobby:gitter.im)

Wren:IG is a community‐developed reverse proxy server providing single sign-on (SSO) to any application behind the proxy.

Wren:IG includes a number of pre-built filters to transform data of intercepred requests / responses.
Scripted filter allows you to easily create your own filter using the Groovy language.

The features of Wren:IG include:

  * Authentication / Authorization using industry-standard protocols (SAML 2.0, OAuth 2.0, OpenID Connect)
  * Single Sign-On (SSO)
  * Single Logout (SLO)
  * Session management
  * Password replay

Wren:IG is one of the projects in the Wren Security Suite, a community initiative that adopted open‐source projects
formerly developed by ForgeRock.

## Contributions

[![Contributing Guide](https://img.shields.io/badge/Contributions-guide-green.svg?style=flat)][contribute]
[![Contributors](https://img.shields.io/github/contributors/WrenSecurity/wrenig)][contribute]
[![Pull Requests](https://img.shields.io/github/issues-pr/WrenSecurity/wrenig)][contribute]
[![Last commit](https://img.shields.io/github/last-commit/WrenSecurity/wrenig.svg)](https://github.com/WrenSecurity/wrenig/commits/main)

## Getting the Wren:IG

You can get Wren:IG Web Application Archive (WAR) in couple of ways:

### Download binary release

The easiest way to get the Wren:IG is to download the latest binary [release](https://github.com/WrenSecurity/wrenig/releases).

### Build the source code

In order to build the project from the command line follow these steps:

**Prepare your Environment**

Following software is needed to build the project:

| Software  | Required Version |
| --------- | -------------    |
| OpenJDK   | 17 and above     |
| Git       | 2.0 and above    |
| Maven     | 3.0 and above    |

**Build the source code**

All project dependencies are hosted in JFrog repository and managed by Maven, so to build the project simply execute Maven *package* goal.

```
$ cd $GIT_REPOSITORIES/wrenig
$ mvn clean package
```

Built binary can be found in `${GIT_REPOSITORIES}/wrenig/openig-war/target/wrenig-${VERSION}.war`.

### Docker image

You can also run Wren:IG in a Docker container. Official Wren:IG Docker images can be found [here](https://hub.docker.com/r/wrensecurity/wrenig).

## Documentation

Project documentation can be found in our documentation platform ([docs.wrensecurity.org](https://docs.wrensecurity.org/wrenig/latest/index.html)).

Documentation is still work in progress.

## Acknowledgments

Wren:IG is standing on the shoulders of giants and is a continuation of a prior work:

* OpenIG by ForgeRock AS

We'd like to thank them for supporting the idea of open-source software.

## Disclaimer

Please note that the acknowledged parties are not affiliated with this project.
Their trade names, product names and trademarks should not be used to refer to
the Wren Security products, as it might be considered an unfair commercial
practice.

Wren Security is open source and always will be.

[contribute]: https://github.com/WrenSecurity/wrensec-docs/wiki/Contributor-Guidelines
