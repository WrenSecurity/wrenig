<p align="center">
  <img src="https://github.com/WrenSecurity/wrenig/assets/13997406/29b9b12e-26df-4057-bcce-ff6fd11c4397">
</p>

# Wren:IG

[![Organization Website](https://img.shields.io/badge/organization-Wren_Security-c12233)](https://wrensecurity.org)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/WrenSecurity)
[![License](https://img.shields.io/badge/license-CDDL-blue.svg)](https://github.com/WrenSecurity/wrenig/blob/main/LICENSE)
[![Source Code](https://img.shields.io/badge/source_code-GitHub-6e40c9)](https://github.com/WrenSecurity/wrenig)
[![Contributing Guide](https://img.shields.io/badge/contributions-guide-green.svg)](https://github.com/WrenSecurity/wrensec-docs/wiki/Contributor-Guidelines)

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

# How to use this image

You can run Wren:IG through this command:

    docker run --rm --name wrenig-test -p 8080:8080 wrensecurity/wrenig:latest

Then you can hit http://localhost:8080/wrenig/studio in your browser.

# Acknowledgments

Wren:IG is standing on the shoulders of giants and is a continuation of a prior work:

* OpenIG by ForgeRock AS

We'd like to thank them for supporting the idea of open-source software.

# Disclaimer

Please note that the acknowledged parties are not affiliated with this project.
Their trade names, product names and trademarks should not be used to refer to
the Wren Security products, as it might be considered an unfair commercial
practice.

Wren Security is open source and always will be.
