FROM --platform=$BUILDPLATFORM debian:bullseye-slim AS project-build

# Install build dependencies
RUN \
  apt-get update && \
  apt-get install -y --no-install-recommends openjdk-17-jdk maven unzip chromium git && \
  # Workaround Chromium binary path for arm64 (see https://github.com/puppeteer/puppeteer/blob/v4.0.0/src/Launcher.ts#L110)
  ln -s /usr/bin/chromium /usr/bin/chromium-browser

# Configure headless Chromium for Puppeteer
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium
# Use '--no-sandbox' option for Puppeteer's Chromium because of incompatibility with Docker
ENV DISABLE_PUPPETEER_SANDBOX=true

# Copy project files
WORKDIR /project
COPY . .

# Perform actual Wren:IG build
ARG BUILD_ARGS
RUN \
  --mount=type=cache,target=/root/.m2 \
  --mount=type=cache,target=/root/.npm \
  mvn package ${BUILD_ARGS}

# Copy built artifact into target directory
RUN \
  --mount=type=cache,target=/root/.m2 \
  mkdir /build && \
  mvn -Dexpression=project.version -q -DforceStdout help:evaluate > /build/version.txt && \
  unzip openig-war/target/wrenig-$(cat /build/version.txt).war -d /build/wrenig


FROM tomcat:9-jdk17-temurin

# Set environment variables
ENV \
  WRENIG_HOME="/srv/wrenig" \
  JAVA_OPTS=" \
    -Dopenig.base=/srv/wrenig \
  " \
  CATALINA_OPTS="-server -Xmx512m"

ARG WRENIG_UID=1000

# Deploy wrenig project
COPY --from=project-build /build/wrenig /usr/local/tomcat/webapps/ROOT

USER ${WRENIG_UID}
WORKDIR ${WRENIG_HOME}

# Prepare Wren:IG configuration directory
RUN mkdir -p $WRENIG_HOME
VOLUME $WRENIG_HOME

CMD ["catalina.sh", "run"]
