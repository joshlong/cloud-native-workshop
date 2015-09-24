
cd config-service
cf push
# run cf apps to get the URL of config-service,
# note it and place it in the next line where it says _URL_
cf cups config-service -p '{"uri":"http://_URL_"}'
cd ..

cd eureka-service
cf push
# run cf apps to get the URL of eureka-service,
# note it and place it in the next line where it says _URL_
cf cups eureka-service -p '{"uri":"http://_URL_"}'
cd ..

cd reservation-service
cf push
cd ..

cd reservation-client
cf push
cd ..
