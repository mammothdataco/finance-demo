// Seed the Data Grid with stock information. This can be run in the
// spark-shell in lieu of packaging it as a standalone job. 

val script = "./scrape.rb"
val tickers = sc.parallelize(List("RHT", "HDP", "KO", "GE", "BBT", "FB", "AAPL", "DUK", "GOOG", "INTC", "TGT"))
val stockInformation = tickers.pipe(script).collect() 
