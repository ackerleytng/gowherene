const labels = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
var map;
var markers = [];

function initMap() {
  const singapore = {lat: 1.352083, lng: 103.819836};
  map = new google.maps.Map(document.getElementById('map'), {
    zoom: 11,
    center: singapore
  });
}

function plotHandler(e) {
  e.preventDefault();
 
  const url = $('input#url').val();
  
  console.log(["request", url]);
  
  $.getJSON("/parse", {"url": url}, function(data) {
    console.log(["response", data]);
    
    markers.map(function(m) { m.setMap(null); });

    markers = data.map(function(item, i) {
      var infowindow = new google.maps.InfoWindow({
        content: `<p><b>${item["place"]}</b></p>` +
          `<p>${item["address"]}</p>`
      });
      var marker = new google.maps.Marker({
        position: item["latlng"],
        label: labels[i % labels.length],
        map: map,
        title: item["place"]
      });
      marker.addListener('click', function() {
        infowindow.open(map, marker);
      });
      
      return marker;
    });
  });
  
  return false;
}

$(document).ready(function() {
    $('a#plot').click(plotHandler);
});
