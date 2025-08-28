# package skip test
mvn clean package -DskipTests
# or
mvn clean package spring-boot:repackage -DskipTests

# run
cd /www/wwwroot/be-tool-phim
nohup java -jar identity-service-0.0.1.jar > app.log 2>&1 &
# nohup = không bị kill khi đóng SSH.

# > app.log 2>&1 & = chạy nền, ghi log ra file app.log.

# Xem log:
# bash
tail -f app.log
# Dừng xem log: Ctrl + C 

# Nếu bạn muốn xem log tạm thời rồi thoát luôn thì có thể dùng:
tail -n 50 app.log

# Dừng app
# bash
ps -ef | grep java
kill -9 <PID>