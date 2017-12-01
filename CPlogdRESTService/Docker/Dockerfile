FROM jetty:9-jre8
#FROM jetty:9.2-jre7

LABEL maintainer "staffan.arvidsson@farmbio.uu.se"

COPY *.war /var/lib/jetty/webapps/root.war

COPY dist/ /var/lib/jetty/webapps/api

EXPOSE 8080