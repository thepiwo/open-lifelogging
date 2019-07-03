const fs = require('fs');

const data_before = fs.readFileSync('./to_sql.json', 'utf8');
const data_fix_json = '[' + data_before.replace(/}{/g, '},{') + ']';
fs.writeFileSync('to_sql.json', data_fix_json, 'utf8');

const data = require('./to_sql.json');

let insert_header = 'INSERT INTO logs ("user_id", "key", "created_at", "data", "hash", "created_at_client") VALUES\n';

let insert_lines = data.map(l => {
  let timestamp = `TIMESTAMP 'epoch' + ${l.createdAtClient} * INTERVAL '1 millisecond'`;
  let data = JSON.stringify(l.data);
  return `(1, '${l.key}', ${timestamp}, '${data}'::JSONB, encode(digest('${data}', 'sha256'), 'hex'), ${timestamp})`
})

let insert = insert_header + insert_lines.join(',\n')

fs.writeFileSync('sql.sql', insert, 'utf8');
