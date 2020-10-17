const fs = require('fs');

const data = JSON.parse(fs.readFileSync('./Location History.json', 'utf8')).locations;

let insert_lines = data.filter(l => {

  // timestamp of last import
  return l.timestampMs > 1595174127466;
});

fs.writeFileSync('google.json', JSON.stringify({locations: insert_lines}, null, 2), 'utf8');
