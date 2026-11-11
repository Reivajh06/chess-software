# ArchChess backend + bundled web frontend.
# Build the Java artifacts first (see README, "Docker"):
#   mvn -DskipTests package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:25-jre

# Set to true to install a Linux Stockfish for the (optional) stockfish.jar plugin.
ARG INSTALL_STOCKFISH=false

RUN if [ "$INSTALL_STOCKFISH" = "true" ]; then \
        apt-get update && apt-get install -y --no-install-recommends stockfish \
        && rm -rf /var/lib/apt/lists/*; \
    fi

WORKDIR /app

COPY target/classes    /app/classes
COPY target/dependency /app/lib
COPY docker/archchess.properties /app/archchess.properties

# Database, datalake (game history) and plugin jars live here; mount a volume to keep them.
RUN mkdir -p /data/plugins
VOLUME /data

# Used by the stockfish plugin; harmless when the plugin is not installed.
ENV STOCKFISH_PATH=/usr/games/stockfish

EXPOSE 5000

ENTRYPOINT ["java", "-cp", "/app/classes:/app/lib/*", "reivajh06.archchess.Launcher", "/app/archchess.properties"]
