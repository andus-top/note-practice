###### java

1. java8计算天数差

   Long diff = ChronoUnit.DAYS.between(LocalDate ld1,LocalDate ld2);

2. java.util.Date 转 java.time.LocalDate

   Instant instant = new Date().toInstant();

   ZoneId zone = ZoneId.systemDefault();

   LocalDateTime ldt = LocalDateTime.ofInstant(instant);

   LocalDate ld = ldt.toLocalDate();

3. Object... values 参数会将List 分组成 [[]]，而不是[]

4. java 时间比较效率

   Integer date = Integer.valueOf(new SimpleDateFormat(“yyyyMMdd”).format(new SimpleDateFormat(“yyyy-MM-dd”).parse("2018-08-30"))); 

   Integer date1 = Integer.valueOf(new SimpleDateFormat(“yyyyMMdd”).format(new SimpleDateFormat(“yyyy-MM-dd”).parse("2018-08-31"))); 

   效率更高: "2018-08-30".compareTo("2018-08-31")

5. ```jsp
   <c:if test="${model.statType == 4}"> 不是 <c:if test="${model.statType} == 4">
   ```