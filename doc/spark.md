###### spark-1.6.0

1. scala.collection.Seq<String> joinCols 作为连接字段后，不会存在重复的列,所以不能用某一个Dataset<Row>去查询列

   Seq<String> joinCols = JavaConversions.asScalaBuffer(java.util.List<String>); 

   相反：Dataset<Row>1.col("id").equalTo(Dataset<Row>2.col("id") 做连接条件时 存在两个列名为id的列

2. Dataset<Row>.na().fill(值); // 传入什么类型，替换什么类型的数据

3. except与exceptAll

   DataSet<Row> A.except(DataSet<Row> B) 先将A、B去重，再移除A中与B相同的数据 

   DataSet<Row> A.exceptAll(DateSet<Row> B) 如果一个数据在A中出现x次，在B中出现y次，x>y。那么结果中数据出现x-y次

4. 