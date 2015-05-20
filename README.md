# Financial Modelling With Spark and the Red Hat JBOSS Data Grid

Two financial demos from [Mammoth Data] demonstrating how to integrate the Red Hat JBOSS Data Grid with HortonWorks' HDP 2.2. 

## Price/Earnings Ratio

This demo calculates the [P/E ratio] (stock / earnings per share) for an assortment of stocks. It demonstrates how you can use HDP to seed the Data Grid and then have a web application pull that information out of the Data Grid for further processing.

## Liquidity Risk Monte Carlo

The Spark 1.2.0 job (written in Scala) performs a Monte Carlo simulation of the performance of multiple stocks over a one-day period and calculates the liquidity value at risk (LVaR) for the portfolio at a 95% confidence level. 

It uses HDP 2.2's Spark preview for calculations and the Red Hat JBOSS Data Grid as an input/output key-value store. The Demo webpage allows you to add stocks, change positions, and run the simulation from the browser.

Additionally, an in-development streaming version that integrates with Apache Kafka is included.

[Mammoth Data]: http://mammothdata.com