<%@ page import="com.where.stats.SingletonStatsCollector" %>
<%@ page import="com.google.common.collect.Ordering" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
      <!--<meta http-equiv="refresh" content="10" >-->
      <title>Wheres my Tube Diagnostics page</title>
  </head>
  <body>

  Current local time is <%= DateFormat.getInstance().format(Calendar.getInstance().getTime())%><br/>
  Current GMT time is <%= DateFormat.getInstance().format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime())%><br/>

  Singleton collector loaded on <%= SingletonStatsCollector.getInstance().getLoadedAt()%><br/>
  Singleton collector created on <%= SingletonStatsCollector.getInstance().getCreatedAt()%><br/>
  <p>
  Total server requests <%= SingletonStatsCollector.getInstance().allShallowHits()%><br/>
  <p>
  <%
      Calendar ealriestDate = Calendar.getInstance();
      Map<String,Deque<SingletonStatsCollector.BranchIterationsStats>> allStats = SingletonStatsCollector.getInstance().allStats();

      int allscrapes = 0;
      int alltrains = 0;
      int alltflHits = 0;
      int allcacheHits = 0;
      long allfirstTime = 0;
      long allsecondTime = 0;
      long alltotalTime = 0;

      for(String branch : Ordering.natural().sortedCopy(allStats.keySet())){
          Deque<SingletonStatsCollector.BranchIterationsStats> allIterationStats = allStats.get(branch);
          %><h2><%=branch%></h2>
            <table border="1">
                <tr>
                    <td width="70">Scrapes</td>
                    <td width="70">Trains</td>
                    <td width="70">TFL hits</td>
                    <td width="70">Cache hits</td>
                    <td width="70">1st Time</td>
                    <td width="70">2nd Time</td>
                    <td width="70">Total time</td>
                    <td width="70">error</td>
                    <td width="150">completion time</td>
                </tr>

                <%
                        int totalscrapes = 0;
                        int totaltrains = 0;
                        int totaltflHits = 0;
                        int totalcacheHits = 0;
                        long totalfirstTime = 0;
                        long totalsecondTime = 0;
                        long totaltotalTime = 0;

                    for(SingletonStatsCollector.BranchIterationsStats iterationStats: allIterationStats){
                        int scrapes = iterationStats.getStats().size();
                        int trains = iterationStats.getNumberOfTrainsFound();
                        int cacheHits = iterationStats.getCacheHits();
                        int tflHits = scrapes - iterationStats.getCacheHits();
                        long firstTime = iterationStats.timeTookForFirstDir();
                        long secondTime = iterationStats.timeTookForSecondDir();
                        long totalTime = iterationStats.totalTimeTook();
                        Calendar completion = iterationStats.completionGmtCompletionTime();
                        String error = iterationStats.errorFromFirstIter();

                        if (completion.before(ealriestDate)) {
                          ealriestDate = completion;
                        }

                        totalscrapes+=scrapes;
                        totaltrains+=trains;
                        totalcacheHits+=cacheHits;
                        totaltflHits+=tflHits;
                        totalfirstTime+=firstTime;
                        totalsecondTime+=secondTime;
                        totaltotalTime+=totalTime;

                        %>
                          <tr>
                            <td width="70"><%=scrapes%></td>
                            <td width="70"><%=trains%></td>
                            <td width="70"><%=tflHits%></td>
                            <td width="70"><%=cacheHits%></td>
                            <td width="70"><%=firstTime%></td>
                            <td width="70"><%=secondTime%></td>
                            <td width="70"><%=totalTime%></td>
                            <td width="70"><%=error==null || error.equals("No Error")?" - ":error%></td>                              
                            <td width="150"><%=DateFormat.getDateTimeInstance().format(completion.getTime())%></td>
                         </tr>
                        <%
                    }

                            allscrapes += totalscrapes;
                            alltrains += totaltrains;
                            alltflHits += totaltflHits;
                            allcacheHits += totalcacheHits;
                            allfirstTime += totalfirstTime;
                            allsecondTime += totalsecondTime;
                            alltotalTime += totaltotalTime;

                    %>
                    <tr>
                        <td width="70">TOTALS</td>
                        <td width="70"></td>
                        <td width="70"></td>
                        <td width="70"></td>
                        <td width="70"></td>
                        <td width="70"></td>
                        <td width="70"></td>
                        <td width="70"></td>
                        <td width="150"></td>
                    </tr>
                      <tr>
                        <td width="70"><%=totalscrapes%></td>
                        <td width="70"><%=totaltrains%></td>
                        <td width="70"><%=totaltflHits%></td>
                        <td width="70"><%=totalcacheHits%></td>
                        <td width="70"><%=totalfirstTime%></td>
                        <td width="70"><%=totalsecondTime%></td>
                        <td width="70"><%=totaltotalTime%></td>
                        <td width="70"></td>                              
                        <td width="150"></td>

                     </tr>
                    </table> <br/><br/>
                    <%
                }
                %>
  <br/><br/>
  <h2>Totals</h2>
  <table border="1">
    <tr>
        <td width="70">Scrapes</td>
        <td width="70">Trains</td>
        <td width="70">TFL hits</td>
        <td width="70">Cache hits</td>
        <td width="70">1st Time</td>
        <td width="70">2nd Time</td>
        <td width="70">Total time</td>
    </tr>

   <tr>
        <td width="70"><%=allscrapes%></td>
        <td width="70"><%=alltrains%></td>
        <td width="70"><%=alltflHits%></td>
        <td width="70"><%=allcacheHits%></td>
        <td width="70"><%=allfirstTime%></td>
        <td width="70"><%=allsecondTime%></td>
        <td width="70"><%=alltotalTime%></td>
     </tr>
   </table>

  <br/>
      <%
        long earliest = ealriestDate.getTime().getTime();
        double secs = new Double(System.currentTimeMillis() - earliest) / 1000.0d;
        double mins = secs/60.0d;

          DecimalFormat nf = new DecimalFormat("0.##");
          double tflHitsPerSec = ((double)alltflHits) / secs;
          double tflHitsPerMin = ((double)alltflHits) / mins;
          double tflHitsPerHour = tflHitsPerMin * 60.0d;
      %>
  <p>Earlest measurement was taken on <%=DateFormat.getDateTimeInstance().format(ealriestDate.getTime())%></p>
  <p>TFL hits per second was <%=tflHitsPerSec%></p>
  <p>TFL hits per minute was <%=nf.format(tflHitsPerMin)%></p>
  <p>TFL hits per hour was <%=nf.format(tflHitsPerHour)%></p>
  <br/>

  <p>total: <%=(new Double(Runtime.getRuntime().totalMemory()))/(1024.0*1024.0)%>m</p>
  <p>free: <%=(new Double(Runtime.getRuntime().freeMemory()))/(1024.0*1024.0)%>m</p>
  <p>max: <%=(new Double(Runtime.getRuntime().maxMemory()))/(1024.0*1024.0)%>m</p>
  <p>used: <%=(new Double(Runtime.getRuntime().totalMemory()) - new Double(Runtime.getRuntime().freeMemory()))/(1024.0*1024.0)%>m</p>

  <p>stack trace:</p>
        <%
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for(StackTraceElement el : stackTraceElements){
                %>
                <%= el.getClassName()%>.<%= el.getMethodName()%><br/> 
                <%
            }
        %>
  </body>
</html>