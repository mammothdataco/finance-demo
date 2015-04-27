# Liquidity Risk Monte Carlo

The Spark 1.2.0 job (written in Scala) performs a Monte Carlo simulation of the performance of multiple stocks over a one-day period and calculates the liquidity value at risk (LVaR) for the portfolio at a 95% confidence level. 

It uses HDP 2.2 platform's Spark preview for calculations and the Red Hat JBOSS Data Grid as an input/output key-value store. The Demo webpage allows you to add stocks, change positions, and run the simulation from the browser.

## Demo

Demo can be found at: http://104.154.61.134:4567/montecarlo.html

* 100,000 LVaR simulations per stock

* Data current as of 2015/04/06 close

* Data Grid holds historical stock and bid-ask spread information for stocks

* Information from Yahoo! Finance and Marketwatch

* You can edit the 'Position' field in the table and the Data Grid will
  be updated with that value for the next simulation run

* (note: don't press enter in the table fields. Just enter the number)

* Given a ticker symbol, the web page will attempt to import historical information into the Data Grid and the stock can then be used for the next simulation

* There's not a huge amount of error-checking - attempting to enter a non-number in the 'Position' field will make that cell turn red, and if an import for a ticker fails (e.g. the ticker doesn't exist), it will also turn red. Oh, and it doesn't check to see if a ticker exists before it adds it to the table.

* Symbols I've tested: AAPL, KO, FB, INTC, DIS


## Compiling & Submitting

To build the simulator, use sbt 0.13.8:

    sbt assembly

This should produce the JAR ```liquidity-monte-carlo.jar``` in ```target/scala-2.10/```.

The JAR can be submitted to a Spark cluster using ```spark-submit```:

    spark-submit --class com.mammothdata.liquidityrisk.LiquidityRiskMonteCarlo ~/liquidity-monte-carlo.jar

## Rationale

We would like to calculate the liquidity risk for a stock portfolio over a daily horizon. 

### Value at Risk

The [Value at Risk] (VaR) of a stock is the answer to a simple question: "How much could I lose from this stock in a day/week/year?" It normally consists of three components: the time period, the confidence level (usually 95% or 99%), and the potential loss. 

For example, a daily VaR at a 95% confidence level of $10,000 means that that 95% of the time, we should not lose more than $10,000 on our position over the course of a day (but it's the other 5% that kills you).

### Liquidity Risk

Liquidity risk for stocks can be priced as [LVaR] - the liquidity value at risk. This is computed by penalizing the VaR by half of the bid-ask spread of our current position.

## Simulation

We are using a [Monte Carlo] method to explore the possible liquidity risk of our portfolio over a single day. We compute the standard deviations for the bid-ask spread and the stock price using historical data and then use Gaussian probabilities to vary both variables over the course of a single trial. We use these generated values to compute the LVaR of that trial and the process is then repeated (e.g. a million trials per stock). The 95% LVaR is then picked for each stock and summed across the portfolio.

### Details

The simulation is written in Scala. First, we obtain the information for the stocks we are interested in and compute the standard deviation for the bid-ask spread and the stock value across the cluster. Secondly, we map over the list of stocks, running ```trials``` on each stock (with a numberOfTrials parameter that determines how many trials we are going to run). The actual LVaR calculation happens in the ```experiment``` method.

(note: the Spark job is partitioned along the stocks. A partition along trials may give better performance in a real-world setting, as it may avoid some shuffling operations)

We then cache the results of the trials in ```trialsResults``` (as otherwise the trials would be recomputed on further operations, resulting in a completely new set of trials occurring), pick out the 95th percentile and sum the liquidity risks across the stock protfolio.

### Stock Information

Stock information is pulled from an installation of the Red Hat JBOSS Data Grid which has been seeded with historical information for each stock we are simulating. 

### Output

The Scala code writes directly into the Data Grid (into the ```liquidityrisk``` key), where it can be pulled out and displayed in the demo web application.

[VaR]:http://www.investopedia.com/articles/04/092904.asp
[LVaR]: http://www.investopedia.com/articles/trading/11/understanding-liquidity-risk.asp
[Monte Carlo]: http://en.wikipedia.org/wiki/Monte_Carlo_method