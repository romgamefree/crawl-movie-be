# Stage 1: build
# Start with a Maven image that includes JDK 21
FROM maven:3.9.8-amazoncorretto-21 AS build

# Copy source code and pom.xml file to /app folder
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build source code with maven
RUN mvn package -DskipTests

#Stage 2: create image
# Use Ubuntu Jammy with JRE 21 to install Chrome for Selenium
FROM eclipse-temurin:21-jre-jammy

# Install Google Chrome and required libs
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       wget curl gnupg ca-certificates unzip fonts-liberation \
       libasound2 libnss3 libxss1 libx11-6 libxau6 libxext6 libxi6 libxrender1 libxrandr2 \
       libatk-bridge2.0-0 libgtk-3-0 libgbm1 libdrm2 libnspr4 libu2f-udev xdg-utils \
       libxshmfence1 libglu1-mesa \
    && wget -O /tmp/google-chrome-stable_current_amd64.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt-get install -y --no-install-recommends /tmp/google-chrome-stable_current_amd64.deb || apt-get -f install -y \
    && rm -f /tmp/google-chrome-stable_current_amd64.deb \
    && rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/google-chrome

# Set working folder to App and copy compiled file from above step
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Command to run the application (honor JAVA_OPTS)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
