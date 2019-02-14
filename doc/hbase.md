- hbase区分大小写、建议hbase表名大写(与phoenix异步索引有关)
- 常用操作
  - DDL
    - 创建表：create '表名','列簇1','列簇2'
    - 查看表结构：describe '表名'
    - 增加2个列簇：disable '表名'、alter '表名' {NAME=>'列簇',VERSIONS=>3},{NAME=>'列簇',VERSIONS=>3}、enable '表名'
    - 删除列簇：disable '表名'、alter '表名' NAME=>'列簇',METHOD=>'delete'
    - 删除表：disable '表名'、drop '表名'
    - 创建命名空间：create_namespace '名称'
    - 删除命名空间：drop_namespace '名称'
    - 清空表：truncate '表名'。就是先删再建
  - DML
    - 向表中放入数据：put '表名','rowkey','列簇:列名','值'
    - 删除某行某列：delete '表名','rowkey','列簇:列名',时间戳。无时间戳删除该列所有值
    - 删除某行：delete '表名','rowkey'
  - DQL
    - 查看所有表：list
    - 查看表是否存在：exists '表名'
    - 表结构是否允许修改：is+enable '表名'
    - 查看所有命名空间：list_namespace
    - 查询某行数据：get '表名','rowkey'
    - 查询某行某列簇[列]：get '表名','rowkey','列簇[:列名]'
    - 查询全表数据：scan '表名'
    - 获取前3行：scan '表名',{LIMIT=>3}
    - 从指定行开始获取3行数据：scan '表名', {STARTROW=>'rowKey', LIMIT=>3}   
    - 获取指定列所有行数据：scan '表名',{COLUMNS=>'列簇:列名'}  
- hbase shell filter

1. 查看有哪些过滤器：show_filters
2. 值等于2018的行：scan 'np:tb',FILTER=>"ValueFilter(=,'binary:2018')"
3. 值包含2018的行：scan 'np:tb', FILTER=>"ValueFilter(=,'substring:2018')"
4. 列为c1且值包含2018或者等于123：scan 'np:tb',FILTER=>"ColumnPrefixFilter('c1') AND （ValueFilter(=,'substring:2018') OR ValueFilter(=,'substring:123')"
5. rowkey以test开头,前5条：scan 'np:tb', {FILTER=>"PrefixFilter('test')",LIMIT=>5}
6. 从test1开始,找到所有的rowkey以test21开头的:scan 'np:tb', {STARTROW=>'test1', FILTER => "PrefixFilter ('test2')"}
7. 从rowkey以test1开始,到test2结尾：scan 'np:tb', {STARTROW=>'test1', STOPROW=>'test2'}
8. 查询rowkey里面包含t1的:
	​	import org.apache.hadoop.hbase.filter.CompareFilter
	​	import org.apache.hadoop.hbase.filter.SubstringComparator
	​	import org.apache.hadoop.hbase.filter.RowFilter	
	​	scan 'np:tb', {FILTER => RowFilter.new(CompareFilter::CompareOp.valueOf('EQUAL'), SubstringComparator.new('t1'))}
9. 匹配正则表达式：
	​	import org.apache.hadoop.hbase.filter.RegexStringComparator
	​	import org.apache.hadoop.hbase.filter.CompareFilter
	​	import org.apache.hadoop.hbase.filter.SubstringComparator
	​	import org.apache.hadoop.hbase.filter.RowFilter	
	​	scan 'np:tb', {FILTER => RowFilter.new(CompareFilter::CompareOp.valueOf('EQUAL'),RegexStringComparator.new('^user\d+\|ts\d+$'))}

​	

​	

​	