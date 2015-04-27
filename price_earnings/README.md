# Price/Earnings Ratio

This demo calculates the [P/E ratio] (stock / earnings per share) for an assortment of stocks. It demonstrates how you can use HDP to seed the Data Grid and then have a web application pull that information out of the Data Grid for further processing.

## Demo

Demo can be found at: http://104.154.61.134:4567/pe.html

* Simple page only

* Seeded with ticker names

* JavaScript on the page queries the Data Grid on page load, obtaining other information dynamically.

* Information is pulled from Yahoo! Finance, Marketwatch, and the SEC's EDGAR reporting website.

* P/E ratio is then calculated using the stock price and EPS information pulled from the Data Grid.

## Details

The Data Grid is seeded using a small Scala job that uses a piped RDD connected a Ruby script. The script scrapes the various external sources to obtain the information for each ticket and also updates the Data Grid with the new information about the stocks (note that this information is passed back to the Scala code, so it could also be used as a base for further processing). 

[P/E ratio]: http://www.investopedia.com/terms/p/price-earningsratio.asp
[Yahoo! Finance]: http://finance.yahoo.com/
[Marketwatch]: http://www.marketwatch.com/
[EDGAR]: http://www.sec.gov/cgi-bin/browse-edgar