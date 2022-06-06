const fs = require('fs')
const JSONStream = require('JSONStream');
const es = require('event-stream');

const stream = fs.createReadStream('Records.json', {encoding: 'utf8'})

stream
    .pipe(JSONStream.parse('locations.*'))
    .pipe(es.mapSync(function (data) {
            delete data.locationMetadata;
            delete data.activity;
            delete data.wifiScan;
            delete data.activeWifiScan;
            delete data.heading;
            delete data.serverTimestamp;
            delete data.verticalAccuracy;
            delete data.batteryCharging;
            delete data.formFactor;
            delete data.osLevel;
            delete data.platform;
            delete data.platformType;
            delete data.deviceTag;
            delete data.source;
            delete data.inferredLocation;
            delete data.deviceTimestamp;
            data.timestampMs = data.timestampMs || new Date(data.timestamp).getTime()
            if (data.timestampMs) data.timestampMs = data.timestampMs.toString()
            delete data.timestamp;
            return data;
    }))
    .pipe(es.filterSync(function (data) {
        return data.timestampMs > 1622575510551 && data.latitudeE7 && data.longitudeE7;
    }))
    .pipe(JSONStream.stringify(open = '{"locations": [\n', sep = ',\n', close = '\n]}\n'))
    .pipe(fs.createWriteStream('google.json'));

