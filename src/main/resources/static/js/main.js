const searchResults = [];

const downloadStatus = {
    inProgress: false,
    fileName: '',
    filePath: '',
    status: 'UNKNOWN'
};

const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame1) {
    console.log('Connected: ' + frame1);
    stompClient.subscribe('/topic/results', function (frame2) {
        const searchResponse = JSON.parse(frame2.body);

        updateSearchResults(searchResponse.results);
    });

    stompClient.subscribe('/topic/download', function (frame2) {
        const response = JSON.parse(frame2.body);

        updateDownloadProgress(response.fileName, response.filePath, response.status);
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

function downloadFile(fileName, fileUrl) {
    downloadStatus.inProgress = true;

    stompClient.send("/app/download", {}, JSON.stringify({fileName: fileName, fileUrl: fileUrl}));

    setTimeout(() => downloadInProgress = false, 20000);
}

function updateSearchResults(results) {
    searchResults.splice(0, searchResults.length);
    results.forEach(r => searchResults.push(r));
}

function updateDownloadProgress(fileName, filePath, status) {
    downloadStatus.inProgress = false;
    downloadStatus.fileName = fileName;
    downloadStatus.filePath = filePath;
    downloadStatus.status = status;

    $('#verificationModal').modal()
}

const app = new Vue({
    el: '#app',
    data: {
        searchResults: searchResults,
        searchText: '',
        downloadStatus: downloadStatus
    },
    methods: {
        search: function () {
            if (!this.searchText || this.searchText === '') return;

            sendSearch(this.searchText);
        },
        download: function (fileName, fileUrl) {
            downloadFile(fileName, fileUrl);
        }
    }
});
