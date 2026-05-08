#!/bin/sh
getPort() {
    proto="$(echo $1 | grep :// | sed -e's,^\(.*://\).*,\1,g')"
    # remove the protocol
    url="$(echo ${1/$proto/})"
    # extract the host
    host="$(echo ${url/$user@/} | cut -d/ -f1)"
    # by request - try to extract the port
    port="$(echo $host | sed -e 's,^.*:,:,g' -e 's,.*:\([0-9]*\).*,\1,g' -e 's,[^0-9],,g')"
    echo "$port"
}

echo "********************************************************"
echo "Waiting for the configuration server to start on port $(getPort $CONFIGSERVER_URI)"
echo "********************************************************"
while ! `nc -z configserver $(getPort $CONFIGSERVER_URI)`; do sleep 3; done
echo "*******  Configuration Server has started"

echo "********************************************************"
echo "Waiting for the mongo server to start on port $(getPort $MONGOSERVER_URI)"
echo "********************************************************"
while ! `nc -z mongodb $(getPort $MONGOSERVER_URI)`; do sleep 3; done
echo "*******  Mongo Server has started"

echo "********************************************************"
echo "Starting Check List Service                           "
echo "Using profile: $PROFILE"
echo "********************************************************"
java $JAVA_OPTIONS -Djava.security.egd=file:/dev/./urandom -Dserver.port=$SERVER_PORT   \
     -Deureka.client.serviceUrl.defaultZone=$EUREKASERVER_URI             \
     -Dspring.cloud.config.uri=$CONFIGSERVER_URI                          \
     -Dspring.profiles.active=$PROFILE                                   \
     -Dsecurity.oauth2.resource.userInfoUri=$AUTHSERVER_URI               \
     -jar /usr/local/checklistservice/$JAR_FILE
