require 'sinatra'
require 'json'
require 'bigdecimal'
require 'csv'
require 'open-uri'
require 'nokogiri'
require 'poseidon'
require 'pry'

$kafka = Poseidon::Producer.new([ENV['KAFKA_HOST']], "finance-demo")

class DataGrid
 
  def initialize
    @auth = 'Authorization: Basic ZGF0YWdyaWQ6UmVkSGF0RGVtbyQy'
    @url = "http://ec2-54-68-56-201.us-west-2.compute.amazonaws.com:8080/rest/default/{{replace}}"
    @read_curl = "curl -H '#{@auth}' -X GET #{@url}"
    @write_curl = "curl -H '#{@auth}' -X PUT #{@url} -H 'Content-type: text/plain' -d '{{value}}'"
  end

  def get(key:)
    cmdline = @read_curl.gsub("{{replace}}", key)
    `#{cmdline}`
  end

  def put(key:, value:)
    cmdline = @write_curl.gsub("{{replace}}", key)
    cmdline.gsub!("{{value}}", value.to_s)
    `#{cmdline}`
  end

  def import(ticker:)
    name = ticker_to_name(ticker: ticker)
    today = "&d=3&e=06&f=2015"
    yahoo_url = "http://ichart.finance.yahoo.com/table.csv?s=#{ticker}#{today}&g=d&a=3&b=12&c=2005&ignore=.csv"
    puts yahoo_url
    historical_data = open yahoo_url
    historical =  CSV.parse(historical_data.read, headers: true )
    put(key: "#{ticker}_stock", value: current_value(historical))
    put(key: "#{ticker}_bidspread", value: current_spread(historical))
    put(key: "#{ticker}_stock_historical", value: historical_stock(historical))
    put(key: "#{ticker}_bidspread_historical", value: historical_spread(historical))
    put(key: "#{ticker}_company", value: name)
    put(key: "#{ticker}_position", value: 0)
    "OK."
  end

  private

  def bidspread(daily) 
    # This is a proxy of the spread as we don't have access to the spread data itself
    ((daily["High"].to_f - daily["Low"].to_f) / daily["High"].to_f) * 100.0
  end

  def historical_spread(historical)
    historical.map {|daily| bidspread(daily) }.to_s.gsub("\ ","").gsub('"',"").slice(1..-2)
  end

  def historical_stock(historical)
    historical.map { |daily| daily["Close"] }.to_s.gsub("\ ","").gsub('"',"").slice(1..-2)
  end

  def current_spread(historical)
    bidspread(historical[0])
  end

  def current_value(historical)
    historical[0]["Close"]
  end

  def ticker_to_name(ticker)
    page = Nokogiri::XML(open "http://www.marketwatch.com/investing/stock/#{ticker[:ticker]}")
    name = page.css("#instrumentname").text
    fail unless name
    name
  end

end

def round(val)
  val = val.to_f
  sprintf "%.2f", val
end

$grid = DataGrid.new


# get '/spark' do
#   submissions = { tickers: params['stocks'], contact: params['contact'], uuid: params['uuid'] }
#   msg = Poseidon::MessageToSend.new("submissions", submissions.to_json)
#   @kafka.send_messages(msg)
#   {status: 'progress'}.to_json
# end

post '/spark' do
  puts JSON.parse(params['submission'])
   submission = params['submission']
   msg = Poseidon::MessageToSend.new("submissions", submission)
   $kafka.send_messages([msg])
#   {status: 'progress'}.to_json
  params['submission']
end

get '/stocks' do
  $grid.get(key: "available_stocks").split(',').to_json
end

get '/ticker/:ticker' do |ticker|
  $grid.get(key: "#{ticker}_stock")
end

get '/import/:ticker' do |ticker|
  $grid.import(ticker: ticker)
end

get '/position/:ticker' do |ticker|
  $grid.get(key: "#{ticker}_position")
end

get '/position/:ticker/:position' do |ticker, position|
  $grid.put(key: "#{ticker}_position", value: position)
end

get '/earnings/:ticker' do |ticker|
  $grid.get(key: "#{ticker}_earnings")
end

get '/name/:ticker' do |ticker|
  $grid.get(key: "#{ticker}_company")
end

get '/datapoints/:uuid' do |uuid|
  datapoints = $grid.get(key: "#{uuid}_datapoints")
  points = datapoints.split(',')
  if points[0][0] == '<'
    404
  else
    points.map(&:to_f).to_json
  end
end

get '/lvar/:uuid' do |uuid|
  lvar = $grid.get(key: "#{uuid}_liquidityrisk")
  if lvar[0] == '<'
    404
  else
    { lvar: lvar.to_f } .to_json
  end
end