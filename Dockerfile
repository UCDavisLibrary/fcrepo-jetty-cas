FROM jetty:9.4.6

RUN apt-get update
RUN apt-get install -y wget

RUN wget https://github.com/fcrepo4/fcrepo4/releases/download/fcrepo-4.7.3/fcrepo-webapp-4.7.3.war
RUN mv fcrepo-webapp-4.7.3.war /var/lib/jetty/webapps/fcrepo.war

RUN mkdir -p /fedora-data
RUN chmod -R a+rw /fedora-data

COPY ./cas-client-integration-jetty.jar /var/lib/jetty/lib/ext/cas-client-integration-jetty.jar
COPY ./cas-client-core.jar /var/lib/jetty/lib/ext/cas-client-core.jar
COPY ./slf4j-simple-1.7.25.jar /var/lib/jetty/lib/ext/slf4j-simple-1.7.25.jar
COPY ./slf4j-api-1.7.25.jar /var/lib/jetty/lib/ext/slf4j-api-1.7.25.jar
COPY ./fcrepo.xml /var/lib/jetty/webapps/fcrepo.xml
COPY ./overlay-web.xml /overlay-web.xml

ENV JAVA_OPTIONS="${JAVA_OPTIONS} -Djava.awt.headless=true -Dfile.encoding=UTF-8 -server -Xms512m -Xmx1024m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -XX:+DisableExplicitGC -Dfcrepo.modeshape.configuration=classpath:/config/file-simple/repository.json -Dfcrepo.home=/fedora-data"