$(window).bind("load", function() {

  $("tbody > tr").each(function(idx, el) { 
    var ticker = el.id;
    
    $.when(get_name(ticker), get_stock_price(ticker), get_earnings(ticker)).done(function () {
      calculate_pe();
    })
  });


  function get_name(ticker) {
    return $.ajax({
      url: 'name/' + ticker,
      type: 'get',
      success: function(data) {
        $("#" + ticker + " .company").text(data);
      }});
  }

  function get_stock_price(ticker) {
    return $.ajax({
      url: 'ticker/' + ticker,
      type: 'get',
      success: function(data) {
        $("#" + ticker + " .stock").text(data);
      }});
  }

  function get_earnings(ticker) {
     return $.ajax({
      url: 'earnings/' + ticker,
      type: 'get',
      success: function(data) {
        $("#" + ticker + " .earnings").text(data);
      }});
  }

  function calculate_pe() {
    $("tbody > tr").each(function(idx, el) {
      var ticker_sym = el.id;

      var stock =  $("#" + ticker_sym + " .stock").text();
      var earnings = $("#" + ticker_sym + " .earnings").text();
      console.log(stock);
      console.log(earnings);
      var pe = (parseFloat(stock) / parseFloat(earnings)).toFixed(2);
      $("#" + ticker_sym + " .pe").text(pe);
    })

  }

});