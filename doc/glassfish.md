###### glassfish-5.0

1. 基本使用

   启动：asadmin start-domain domain1

   停止：asadmin stop-domain domain1

   创建新的domain: asadmin create-domain --adminport 14848 domain2

   在创建命令中直接指定管理台端口号–14848，其余端口号因为被domain1占用而自行选定其它端口（domain1正在运行）。

   删除domain：asadmin delete-domain domain2

2. 