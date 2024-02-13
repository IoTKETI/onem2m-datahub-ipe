java -Dserver.port=8080 \
	-jar ipe-integration-1.0.0-SNAPSHOT.jar > /dev/null 2>&1 &
tail -f logs/ipe.log

