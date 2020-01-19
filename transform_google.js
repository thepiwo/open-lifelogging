const fs = require('fs');

const data = JSON.parse(fs.readFileSync('./google.json', 'utf8')).locations;
let insert_header = 'INSERT INTO logs ("user_id", "key", "created_at", "data", "hash", "created_at_client") VALUES\n';

let insert_lines = data.map(l => {
  let timestamp = `TIMESTAMP 'epoch' + ${l.timestampMs} * INTERVAL '1 millisecond'`;

  let new_data = {
    accuracy: l.accuracy,
    latitude: l.latitudeE7 / 10000000,
    longitude: l.longitudeE7 / 10000000,
  };

  let data = JSON.stringify(new_data);

  return `(1, 'CoordEntity', ${timestamp}, '${data}'::JSONB, encode(digest('${data}', 'sha256'), 'hex'), ${timestamp})`
});

let insert = insert_header + insert_lines.join(',\n');
fs.writeFileSync('google.sql', insert, 'utf8');
