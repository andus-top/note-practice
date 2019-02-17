###### hive-1.2.1

1. hive分页查询（基于row_number函数）

   select dt,count from (

   ​	select row_number() over (order by r.id asc, r.dt desc) as rank,r.dt,count(r.id) as count from tb_user

   ​	group by r.id,r.dt

   ) as t where r.rank >=1 and t.rank <= 1000;

2. hive分组连接

   将id相同的col列值连接起来，以|分割，col列数据会被去重: 

   select concat_ws('|',collect_set(col) from test group by id;

   还有collect_list(col)  不去重