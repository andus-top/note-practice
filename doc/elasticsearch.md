elasticsearch-5.2.2

1. in 查询的坑

   sql：select * from table where <font color=red>cnCountry</font> in (1,2,3,4) 

   java代码：

   SearchRequestBuilder srb= client.prepareSearch(index).setTypes(type).setSearchType(SearchType.QUERY_THEN_FETCH); 

   srb.setQuery(QueryBuilders. <font color=red>termsQuery</font>("<font color=red>cnCountry.keyword</font>", "1","2","3"));]

2. 