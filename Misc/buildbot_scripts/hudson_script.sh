cd $WORKSPACE

# Stop the AnnisService
chmod u+x buildbot_scripts/stopService.sh
./buildbot_scripts/stopService.sh /opt/annis/trunk

# Copy the the Annis webapp
cp annis-gui/target/annis-gui.war /srv/tomcat/annis3.war

# Copy Annis Service
chmod u+x buildbot_scripts/copyService.sh
buildbot_scripts/copyService.sh /opt/annis/trunk

# start service
chmod u+x buildbot_scripts/startService.sh
BUILD_ID=allow_to_run_as_daemon buildbot_scripts/startService.sh /opt/annis/trunk/
