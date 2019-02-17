###### mysql-使用时的一些记录

1. join 相关问题

   join 会按照参照表中数据的顺序去连接

   inner join 如果a，b两表存在都存在连接条件的某个值，且都有2个，那么连接后有4条结果

2. count(*)、count(1)、count(列)

   count()中的表达式是否为NULL，如果为NULL则不计数，而非NULL则会计数。

   实际上如何写Count并没有区别。 某个表上count(*)用的比较多时，考虑在一个最短的列建立一个单列索引，会极大的提升性能。

3. mysql把指定数据排在前面

   order by 列<>'美国' 

   order by 列 not in (1,2,3)

4. 避免重复的记录

   insert ignore 

   replace into 

   insert on duplicate key update

5. set names utf8，仅当前连接有效

   SET character_set_client='utf8'; 

   SET character_set_connection='utf8'; 

   SET character_set_results='utf8';

6. mysql导入sql文件时

   1. 将sql文件保存为uft8格式 
   2. 导入数据之前执行：set names utf8;