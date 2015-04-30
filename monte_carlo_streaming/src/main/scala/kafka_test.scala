import org.json4s._
import org.json4s.native._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._

 

object KafkaTest extends java.io.Serializable {

  case class Tweet(user: String, tttztl: String)
 
 // case class Stocks(symbol: String, position: Int)
 // case class Contact(name: String, phone: String, business: String, email: String, twitter: String)
 // case class Submission(id: String, contact: Contact, stocks: List[Stock])

 // JsonMethods.parse(sample).extract[Submission]

   def main(args: Array[String]) {
    val conf = new SparkConf().setAppName("Kafka Test")
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(1))
    val lines = KafkaUtils.createStream(ssc, "localhost", "0", Map("test" ->  1)).map(_._2)
    val test = lines.foreachRDD(x => x.foreach(rdd => {  
      val tweet = JsonMethods.parse(rdd).extract[Tweet]
      println(tweet.tttztl)
    }))
    ssc.start()
    ssc.awaitTermination()
  }
}


