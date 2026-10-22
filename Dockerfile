# Evie Launcher rework — Android build environment.
# Base ships JDK 17 + Gradle 9.8.1; we add the Android SDK (AGP 9.4.x requires
# Gradle >= 9.6, build-tools >= 36.0.0, JDK 17).
FROM gradle:9.8.1-jdk17-noble

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    PATH=/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:$PATH

USER root

RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip wget ca-certificates \
 && rm -rf /var/lib/apt/lists/*

ARG CMDLINE_TOOLS_ZIP=commandlinetools-linux-11076708_latest.zip

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
 && wget -q "https://dl.google.com/android/repository/${CMDLINE_TOOLS_ZIP}" -O /tmp/clt.zip \
 && unzip -q /tmp/clt.zip -d ${ANDROID_HOME}/cmdline-tools \
 && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
 && rm /tmp/clt.zip

# Accept licenses up-front so first gradle build isn't blocked on interactive prompt.
RUN yes | sdkmanager --licenses > /dev/null \
 && sdkmanager --install \
      "platform-tools" \
      "platforms;android-35" \
      "build-tools;36.0.0" \
 && rm -rf ${ANDROID_HOME}/.temp

USER gradle
WORKDIR /home/gradle/project