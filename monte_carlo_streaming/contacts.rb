require 'poseidon'
require 'json'
require 'csv'


kafka = Poseidon::PartitionConsumer.new("finance-demo-consumer", ENV['KAFKA_HOST'], 9092,
                                            "submissions", 0, 0)

CSV.open("path/to/file.csv", "wb") do |csv|

messages = kafka.fetch
  messages.each do |m|
    details = JSON.parse(m)['contact']
    csv << [details[:name], details[:email], details[:phone]]
  end
end

