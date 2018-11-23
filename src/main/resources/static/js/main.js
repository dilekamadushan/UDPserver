const searchResults = [];

const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame1) {
    console.log('Connected: ' + frame1);
    stompClient.subscribe('/topic/results', function (frame2) {
        const searchResponse = JSON.parse(frame2.body);

        addSearchResult(searchResponse.searchId, searchResponse.searchResult);
    });
});

function uuid() {
    let uuid = "", i, random;
    for (i = 0; i < 32; i++) {
        random = Math.random() * 16 | 0;

        if (i === 8 || i === 12 || i === 16 || i === 20) {
            uuid += "-"
        }
        uuid += (i === 12 ? 4 : (i === 16 ? (random & 3 | 8) : random)).toString(16);
    }
    return uuid;
}

function sendSearch(searchText) {
    searchResults.splice(0, searchResults.length);
    stompClient.send("/app/search", {}, JSON.stringify({searchText: searchText}));
}

function updateSearchResults(results) {
    searchResults.splice(0, searchResults.length);
    results.forEach(searchResults.push);
}

const app = new Vue({
    el: '#app',
    data: {
        searchResults: searchResults,
        searchText: ''
    },
    methods: {
        search: function () {
            if (!this.searchText || this.searchText === '') return;

            sendSearch(this.searchText);
        }
    }
});