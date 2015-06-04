/** Nous irons à Monte Carlo - Liquidity Risk Monte Carlo (over 1-Day horizon)
*   
*   Liquidity Risk - defn: http://www.investopedia.com/articles/trading/11/understanding-liquidity-risk.asp
*   VaR: http://www.investopedia.com/articles/04/092904.asp
* 
*/

package com.mammothdata.liquidityrisk

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import dispatch._, Defaults._
import Array._

object LiquidityRiskMonteCarlo extends java.io.Serializable {

  val datagridURL = "http://ec2-54-68-56-201.us-west-2.compute.amazonaws.com:8080/rest/default/"

  class Stock(val name: String, 
              val stdev: Double,
              val currentValue: Double,
              val numberOfStocks: Int,
              val bidSpreadCurrent: Double,
              val bidSpreadStdev: Double) extends java.io.Serializable {
  }

  // Our experiment - generate a new stock price using a Gaussian distribution
  // over the stock standard distribution. Do the same with the bid spread
  // and calculate the liquidity risk of the stock holding 

  def experiment(stock: Stock): Double = {

    val newStockPrice = stock.currentValue + (stock.stdev * scala.util.Random.nextGaussian())
    val valueAtRisk = (newStockPrice - stock.currentValue) * stock.numberOfStocks
    val bidSpread = stock.bidSpreadCurrent + (stock.bidSpreadStdev * scala.util.Random.nextGaussian())
    val liquidityRisk = (valueAtRisk + (0.5 * ((bidSpread / 100) * (newStockPrice * stock.numberOfStocks))))

    liquidityRisk
  }

  // Run the trials over a stock

  def trials(stock: Stock, numberOfTrials: Int): Array[Double] = {
    val trialRange = range(0, numberOfTrials)
    trialRange.map( x => experiment(stock))
  }

  // LVaR and VaR are often calculated on 95% confidence level,
  // so get the value at 95% 

  def percentile(x: Array[Double]): Double = {
    val experimentsLength = x.length
    val positionof95 = (experimentsLength * 0.95).toInt
    val ordered = x.sorted

    scala.math.abs(ordered((experimentsLength - positionof95)-1))
  }

  def reduceFn(x: Array[Double], y: Array[Double]): Array[Double] = {
    val combined = new Array[Double](x.length)
    for(i <- 0 until x.length) {
      combined(i) = x(i) + y(i)
    }
    combined
  }

   // Get the ticker symbols from the DataGrid

  def tickerSymbolsToSimulate(uuid: String): List[String] = {
    val req = url(datagridURL + uuid +"_stocklist").as_!("datagrid", "RedHatDemo$2")
    val result = (Http(req OK as.String))
    result().split(",").toList
  }

   def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("Liquity Risk Monte Carlo Simulation")
    val sc = new SparkContext(conf)

    val uuid = args(1)

    // Set up stocks

    val tickers = tickerSymbolsToSimulate(uuid)

    println(tickers)

    val stocks = sc.parallelize(tickers.map( ticker => stockDataFor(ticker, sc, uuid)))

    // Run trials and cache the results

    val trialResults = stocks.map( stock => trials(stock, args(0).toInt) )
    trialResults.cache

    // Pull out our 95% confidence values, sum across portfolio
    // for the liquidity risk of the portfolio, and upload into
    // the DataGrid

    val percentile95 = trialResults.map(stock => percentile(stock))
    val lvarOfPortfolio = percentile95.sum

    // Again, this is why we'd split on trial instead of stock symbol
    // outside of a demo implementation

    // val combinedLVaRs = trialResults.map( stock => stock.sorted ).reduce( (x,y) => reduceFn(x,y))

    val liquidityReq = url(datagridURL + uuid + "_liquidityrisk").PUT
                          .as_!("datagrid", "RedHatDemo$2")
                          .setBody(lvarOfPortfolio.toString)
    (Http(liquidityReq OK as.String))                          

    // val resultDataPoints = url(datagridURL + "liquiditydatapoints").PUT
    //                       .as_!("datagrid", "RedHatDemo$2")
    //                       .setBody(combinedLVaRs.mkString(","))

    // (Http(resultDataPoints OK as.String))                           

    sc.stop()

  }

  // Pull stock information for a ticker from the DataGrid

  def stockDataFor(ticker: String, sc: SparkContext, uuid: String): Stock = {

    def obtainStockData(category: String): String = {
      val raw = url(datagridURL + ticker + "_" + category).as_!("datagrid", "RedHatDemo$2")
      println(raw)
      val result = (Http(raw OK as.String))
      result()
    }

    def currentValue: Double = {
      obtainStockData("stock").toDouble
    }

    def numberOfStocks: Int = {
      obtainStockData(uuid + "_position").toInt
    }

    def currentBidSpread: Double = {
      obtainStockData("bidspread").toDouble
    }

    def historicalStockValue = {
      val stocks = obtainStockData("stock_historical")
      sc.parallelize(stocks.split(",").toList.map( x => x.toDouble ))
    }

    def historicalBidSpread = {
      val spreads = obtainStockData("bidspread_historical")
      sc.parallelize(spreads.split(",").toList.map( x => x.toDouble ))
    }

    historicalBidSpread.cache
    historicalStockValue.cache
    val stockStdev = historicalStockValue.stats.stdev
    val bidSpreadStdev = historicalBidSpread.stats.stdev
    
    new Stock(ticker, stockStdev, currentValue, 
              numberOfStocks, currentBidSpread, bidSpreadStdev)

  }
}