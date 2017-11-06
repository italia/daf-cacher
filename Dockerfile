FROM openjdk:8u141-jdk-slim

COPY target/dependency /usr/src/daf-metabase-cacher/dependency
COPY target/*.jar /usr/src/daf-metabase-cacher/
COPY daf-metabase-cacher.sh /usr/bin/daf-metabase-cacher
RUN chmod +x /usr/bin/daf-metabase-cacher
COPY entrypoint.sh /
RUN chmod +x /entrypoint.sh
WORKDIR /usr/src/daf-metabase-cacher
ENTRYPOINT ["/entrypoint.sh"]
CMD ["/usr/bin/daf-metabase-cacher"]
