- phoenix区分大小写，不管输入大写还是小写，phoenix都会把它转化成大写。如果要小写的话必须加上""

- 基本操作

  - DDL

    - 创建表/视图：create table/view phtest(rk varchar primary key,name varchar,age unsigned_int);
    - 删除表/视图：drop table/view 表名;
    - 创建索引：create index 索引名 on schema.表名(列簇.列名,列簇.列名) include(列簇.列名)
    - 删除索引：drop index if exists 索引名 on schema.表名;
    - 重建索引：alter index索引名 on 表名 rebuild;

  - DML

    - 插入/更新数据：

      upsert into phtest values('rowkey','itxiaofen',26); 

      UPSERT INTO PHTEST(RK, AGE) VALUES('rk', 0) ON DUPLICATE KEY UPDATE AGE = AGE + 1;

    - 删除数据：delete from 表名 where rk='rk';

  - DQL

    - 查看所有的表、视图、索引：!tables

    - 查看表结构：!describe 表名/视图/索引;

    - 查询数据：

      select age,count(1) from 表名 group by age ;

      select * from 表名limit 1000 offset 100; # 分页查询，下标从0开始

    - 