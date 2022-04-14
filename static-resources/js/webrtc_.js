let gStream = null;
function hasUserMedia() {
    navigator.getUserMedia = navigator.getUserMedia
        || navigator.webkitGetUserMedia
        || navigator.mozGetUserMedia
        || navigator.msGetUserMedia;
    return !!navigator.getUserMedia;
}

function initUserMedia() {
    if (hasUserMedia()) {
        navigator.getUserMedia = navigator.getUserMedia
            || navigator.webkitGetUserMedia
            || navigator.mozGetUserMedia
            || navigator.msGetUserMedia;

        // create a filter node
        let audioContext;
        if (typeof AudioContext === 'function') {
            audioContext = new AudioContext();
        } else if (typeof webkitAudioContext === 'function') {
            audioContext = new webkitAudioContext(); // eslint-disable-line new-cap
        } else {
            console.log('Sorry! Web Audio not supported.');
        }
        let filterNode = audioContext.createBiquadFilter();
        // see https://dvcs.w3.org/hg/audio/raw-file/tip/webaudio/specification.html#BiquadFilterNode-section
        filterNode.type = 'highpass';
        // cutoff frequency: for highpass, audio is attenuated below this frequency
        filterNode.frequency.value = 10000;

        // create a gain node (to change audio volume)
        let gainNode = audioContext.createGain();
        // default is 1 (no change); less than 1 means audio is attenuated
        // and vice versa
        gainNode.gain.value = 0.5;

        //get both video and audio streams from user's camera
        navigator.getUserMedia({video: { width: 320, height: 240, frameRate: 5 }, audio: true},
                stream => {
            gStream = stream;
            // // Create an AudioNode from the stream
            // const mediaStreamSource = audioContext.createMediaStreamSource(stream);
            // mediaStreamSource.connect(filterNode);
            // filterNode.connect(gainNode);
            // // connect the gain node to the destination (i.e. play the sound)
            // gainNode.connect(audioContext.destination);

            let video = document.getElementById('video');
            //insert stream into the video tag
            // video.src = window.URL.createObjectURL(stream);

            // const videoTracks = stream.getVideoTracks();
            // console.log('Got stream with constraints:', constraints);
            // console.log("Using video device: ${videoTracks[0].label}");
            // window.stream = stream; // make variable available to browser console
            video.srcObject = stream;
        }, err => {
        });

    } else {
        alert("Error. WebRTC is not supported!");
    }
}

$("#btnGetAudioTracks").on("click", function () {
    console.log("getAudioTracks");
    console.log(gStream.getAudioTracks());
});

$("#btnGetTrackById").on("click", function () {
    console.log("getTrackById");
    console.log(gStream.getTrackById(gStream.getAudioTracks()[0].id));
});

$("#btnGetTracks").on("click", function () {
    console.log("getTracks()");
    console.log(gStream.getTracks());
});

$("#btnGetVideoTracks").on("click", function () {
    console.log("getVideoTracks()");
    console.log(gStream.getVideoTracks());
});

$("#btnRemoveAudioTrack").on("click", function () {
    console.log("removeAudioTrack()");
    gStream.removeTrack(gStream.getAudioTracks()[0]);
});

$("#btnRemoveVideoTrack").on("click", function () {
    console.log("removeVideoTrack()");
    gStream.removeTrack(gStream.getVideoTracks()[0]);
});

$("#btnStartUserMedia").on("click", e=>{
    initUserMedia();
});

function initRTCConnection() {
    let peerConn = new RTCPeerConnection();

    //establishing peer connection
    //...
    //end of establishing peer connection
    // let dataChannel = peerConn.createDataChannel("myChannel", dataChannelOptions);

    // here we can start sending direct messages to another peer
}