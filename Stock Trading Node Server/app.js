'use strict';
var express = require('express');
var cors = require('cors');
var axios = require('axios');
const app = express();
const https = require('https');
app.use(cors());


const APIKeyNewsApi = "daeeb78ab82141b18e676dc9fa12d5ca";
const APIKEYTiingo = "16366341e960c528a8d105a02ca16cf5aa902e9c"
// const APIKEYTiingo = "7fbcf684d720c363662cf7d51029a730b58ac17c"

// Index Page!
app.get('/', function (req, res) {
    res.status(200).send("Hello, Welcome to Backend Server Homepage.").end();
});

// Company's Description
app.get('/apis/stocks/companyDetails', function (req, res) {
    let params = req.query;
    let URL = 'https://api.tiingo.com/tiingo/daily/' + params.ticker + '?token=' + APIKEYTiingo;
    console.log(URL);
    https.get(URL, (response) => {
        let data = '';
        response.on('data', (chunk) => {
            data += chunk;
        });
        response.on('end', () => {
            res.send(JSON.parse(data))
        })
    }).on("error", (err) => {
        res.send("Error: " + err.message);
    });
});

// Company's Top-of-Book & Last Price.
app.get('/apis/stocks/lastPrice', function (req, res) {
    let params = req.query;
    let URL = 'https://api.tiingo.com/iex/?tickers=' + params.ticker + '&token=' + APIKEYTiingo;
    console.log(URL);
    axios.get(URL)
        .then(function (response) {
            let retVal = getLastPrice(response.data);
            console.log(`Sending back ${retVal.length} results for ${params.ticker}'s last Price`);
            res.json(retVal);
        })
        .catch(function (error) {
            console.log(error);
            res.json({'error': 'No Data Found.' });
        })
});

function getLastPrice(res){
    let response = []
    for(let i = 0; i < res.length; i++){
        let result = res[i]
        result['change'] = parseFloat((result['last'] - result['prevClose']).toFixed(2));
        result['changePercentage'] = parseFloat((result['change'] * 100 / result['prevClose']).toFixed(2));
        console.log("Timestamp: ", new Date(result['timestamp']), " ", new Date());
        let currentTime = new Date().getTime();
        let lastTime = new Date(result['timestamp']).getTime();
        let seconds = Math.abs(currentTime - lastTime) /  1000;
        console.log(seconds);
        if(seconds >= 60){
            result['marketOpen'] = false;
        }
        else{
            result['marketOpen'] = true;
        }
        response.push(result);
    }
    console.log(response);
    return response;
}

// History
app.get('/apis/stocks/history', function (req, res) {
    let params = req.query;
    let date = getDate(2);
    let URL = 'https://api.tiingo.com/tiingo/daily/' + params.ticker + '/prices?startDate='+date+'&token=' + APIKEYTiingo;
    console.log(URL);
    https.get(URL,
        function (response) {
            let data = '';
            response.on('data', (chunk) => {
                data += chunk;
            });
            response.on('end', () => {
                res.send(extractHistory(JSON.parse(data)));
            })
        }
    );
});

function extractHistory(data){
    let response = {'ohlc': [], 'volume': []}
    let responseOhlc = []
    let responseVol = []
    for(let i=0; i < data.length; i++){
        let res1 = []
        let res2 = []
        data[i]['date'] = formatDateForChart(data[i]['date']);
        res1.push(data[i]['date']);
        res1.push(data[i]['open']);
        res1.push(data[i]['high']);
        res1.push(data[i]['low']);
        res1.push(data[i]['close']);
        responseOhlc.push(res1);
        res2.push(data[i]['date']);
        res2.push(data[i]['volume']);
        responseVol.push(res2);
    }
    response["ohlc"] = responseOhlc;
    response["volume"] = responseVol;

    return response;
}

function formatDateForChart(formatDate){
    let d = Date.parse(formatDate);
    return d;
}

function getDate(yearsAgo){
    let date_ob = new Date();

    let date = ("0" + date_ob.getDate()).slice(-2);

    let month = ("0" + (date_ob.getMonth() + 1)).slice(-2);
    let year = date_ob.getFullYear() - yearsAgo;
    return `${year}-${month}-${date}`;
}

app.get('/apis/stocks/lastChartPrices', function (req, res){
    let params = req.query;
    let date = params.timestamp.substr(0,10);
    let URL = 'https://api.tiingo.com/iex/'+ params.ticker +'/prices?startDate='+date+'&resampleFreq=4min&token=' + APIKEYTiingo;
    https.get(URL,
        function (response) {
            let data = '';
            response.on('data', (chunk) => {
                data += chunk;
            });
            response.on('end', () => {
                res.send(extractDailyHistory(JSON.parse(data)));
            })
        }
    );
});

function extractDailyHistory(data){
    let response = []
    for(let i=0; i < data.length; i++){
        let res1 = []
        data[i]['date'] = formatDateForChart(data[i]['date']);
        res1.push(data[i]['date']);
        res1.push(data[i]['open']);
        res1.push(data[i]['high']);
        res1.push(data[i]['low']);
        res1.push(data[i]['close']);
        response.push(res1);
    }
    return response;
}

// Autocomplete
app.get('/apis/stocks/autocomplete', function (req, res) {
    let params = req.query;
    let query = params.query
    console.log(`GET request recieved for searching ${query}`);
    axios.get(`https://api.tiingo.com/tiingo/utilities/search?query=${query}&token=${APIKEYTiingo}`)
        .then(function (response) {
            console.log('Successful');
            let results = response.data;
            let retVal = extractSearchResult(results)
            console.log(`Sending back ${retVal.length} results for searching ${query}`);
            res.json(retVal);
        })
        .catch(function (error) {
            console.log(error);
        })
})

function extractSearchResult(results) {
    let retVal = []
    for (let i = 0; i < results.length; i++) {
        let result = results[i];
        let searchResult = {};
        searchResult.ticker = result.ticker;
        searchResult.name = result.name;
        retVal.push(searchResult)
    }
    return arrayContainsAll(retVal, ["ticker", "name"])
}


app.get('/apis/news/:ticker', function (req, res) {
    let ticker = req.params.ticker;
    console.log(`GET request received for ${ticker}'s news`);
    console.log(`https://newsapi.org/v2/everything?apiKey=${APIKeyNewsApi}&q=${ticker}`);
    axios.get(`https://newsapi.org/v2/everything?apiKey=${APIKeyNewsApi}&q=${ticker}`)
        .then(function (response) {
            console.log('Successful');
            let articles = response.data.articles;
            let retVal = extractNewsArticles(articles);
            console.log(`Sending back ${retVal.length} results for ${ticker}'s news`);
            res.json(retVal);
        })
        .catch(function (error) {
            console.log(error);
        })
});

function extractNewsArticles(articles) {
    let retVal = []
    for (let i = 0; i < articles.length; i++) {
        let article = articles[i];
        let result = {};
        result.url = article.url;
        result.title = article.title;
        result.source = article.source.name;
        result.urlToImage = article.urlToImage;
        result.publishedAt = article.publishedAt;
        result.description = article.description;
        result.content = article.content;
        retVal.push(result)
    }
    return arrayContainsAll(retVal, ["url", "title", "source", "urlToImage", "publishedAt", "description", "content"])
}


function arrayContainsAll(dataArray, fields) {
    return dataArray.filter(data => containsAll(data, fields))
}

function containsAll(data, fields) {
    for (let i = 0; i < fields.length; i++) {
        let field = fields[i]
        if (!data[field]) {
            return false
        }
    }
    return true
}

// Start the server
const PORT = process.env.PORT || 8081;
app.listen(PORT, () => {
    console.log(`App listening on port ${PORT}`);
    console.log('Press Ctrl+C to quit.');
});

module.exports = app;
