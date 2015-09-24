
cd config-service
cf push
# run cf apps to get the URL of config-service,
# or note it from the "urls" output of the `cf push`
# and place it in the next line where it says _URL_
cf cups config-service -p '{"uri":"http://config-service-unfiercely-alkalimetry.cfapps.io"}'
cd ..

cd eureka-service
cf push
# run cf apps to get the URL of eureka-service,
# or note it from the "urls" output of the `cf push`,
# and place it in the next line where it says _URL_
cf cups eureka-service -p '{"uri":"http://eureka-service-rooted-subquarter.cfapps.io"}'
cd ..

cd reservation-service
cf push
cd ..

cd reservation-client
cf push
cd ..
