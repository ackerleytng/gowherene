function plotHandler(e) {
  e.preventDefault();
 
  var url = $('input#url').val();
  
  console.log(["request", url]);
  
  $.get("/parse", {"url": url}, function(data) {
    console.log(["response", data]);
  });
  
  return false;
}

$(document).ready(function() {
    $('a#plot').click(plotHandler);
});
