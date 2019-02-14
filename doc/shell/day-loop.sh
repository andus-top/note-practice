#!/bin/sh
# 开始日期-结束日期-按天循环脚本
datebeg=$1 # 第一参数
dateend=$2 # 第二个参数
beg_s=`date -d "$datebeg" +%s`
end_s=`date -d "$dateend" +%s`
echo $beg_s
echo $end_s
while [ "$beg_s" -le "$end_s" ]
do
	now=`date +%Y-%m-%d:%H:%M:%S`
    echo "exe at :"$now # 2018-08-29 12:30:54
	sudo /usr/local/spark-2.4.0-bin-hadoop2.7/bin/spark-submit --class com.ysl.Test --name 'test' --master yarn --driver-memory 1g --executor-memory 2g --executor-cores 2 --deploy-mode cluster --jars hdfs:///user/root/spark/spark-2.4/lib/user_lib/*.jar /home/ysl/test.jar --FILE_DATE=$(date -d @$beg_s +"%Y-%m-%d")
	echo "exe tarDay :"$(date -d @$beg_s +"%Y-%m-%d") # 2018-08-29
	beg_s=$((beg_s+86400))
done
