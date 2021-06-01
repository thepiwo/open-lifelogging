const fs = require('fs')
const JSONStream = require('JSONStream');
const es = require('event-stream');

const stream = fs.createReadStream('Location History.json', {encoding: 'utf8'})

stream
    .pipe(JSONStream.parse('locations.*'))
    .pipe(es.mapSync(function (data) {
        delete data.locationMetadata;
        delete data.activity;
        delete data.wifiScan;
        delete data.platform;
        return data;
    }))
    .pipe(es.filterSync(function (data) {
        return data.timestampMs > 1622575510551 && data.latitudeE7 && data.longitudeE7;
    }))
    .pipe(JSONStream.stringify(open = '{"locations": [\n', sep = ',\n', close = '\n]}\n'))
    .pipe(fs.createWriteStream('google.json'));

