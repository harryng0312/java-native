//connecting to our signaling server 
// let conn = new WebSocket('ws://localhost:9090/socket');
let add = $.trim($("#add").prop("innerHTML"));
let protocol = $.trim($("#protocol").prop("innerHTML"));
let conn = new WebSocket(protocol + "://" + add + "/ws/socket");
let stunAdds = [//{"urls": ["stun:iphone-stun.strato-iphone.de:3478"]},
    {
        "urls": ["stun:rhel-php:3478", "turn:rhel-php:3478"],
        "username": "username",
        "credential": "password"
    }];
let peerConnection;
let sendDataChannel;
let receiveDataChannel;
let input = document.getElementById("messageInput");

conn.onopen = function () {
    console.log("Connected to the signaling server");
    initialize();
}

conn.onmessage = async function (msg) {
    console.log("Got message", msg.data);
    let content = JSON.parse(msg.data);
    let data = content.data;
    switch (content.event) {
        // when somebody wants to call us
        case "offer":
            await handleOffer(data);
            break;
        case "answer":
            await handleAnswer(data);
            break;
        // when a remote peer sends an ice candidate to us
        case "candidate":
            await handleCandidate(data);
            break;
        default:
            break;
    }
}

function send(message) {
    conn.send(JSON.stringify(message));
}

function initialize() {
    let configuration = {"iceServers": stunAdds};
    // let configuration = null;
    let RTCPeerConnection = window.RTCPeerConnection || window.webkitRTCPeerConnection || window.mozRTCPeerConnection;
    // peerConnection = new RTCPeerConnection(configuration, {
    //     optional: [{
    //         RtpDataChannels: true
    //     }]
    // });
    peerConnection = new RTCPeerConnection(configuration);

    // Setup ice handling
    peerConnection.onicecandidate = function (event) {
        if (event.candidate) {
            send({
                event: "candidate",
                data: event.candidate
            });
        }
    };

    peerConnection.onicecandidateerror = function (evt) {
        console.log("ICE Candidate err:" + evt);
    }

    peerConnection.onnegotiationneeded = async evt => {
        // await createOffer();
        // let offer = await peerConnection.createOffer({
        //     offerToReceiveAudio: true,
        //     offerToReceiveVideo: true
        // })
        // await peerConnection.setLocalDescription(offer);
    }
    // creating data channel
    // dataChannel = peerConnection.createDataChannel("dataChannel", {
    //     reliable: true
    // });
    // let dataChannelOptions = {
    //     reliable: true,
    //     maxRetransmitTime: "2000"
    // };
    let handleChannelCallback = function (event) {
        receiveDataChannel = event.channel;
        receiveDataChannel.onopen = dataChannelOpen;
        receiveDataChannel.onclose = dataChannelClose;
        receiveDataChannel.onmessage = dataChannelMessage;
        receiveDataChannel.onerror = dataChannelError;
    };
    peerConnection.ondatachannel = handleChannelCallback;
    sendDataChannel = peerConnection.createDataChannel("dataChannel");
    sendDataChannel.onopen = dataChannelOpen;
    sendDataChannel.onclose = dataChannelClose;
    sendDataChannel.onmessage = dataChannelMessage;
    sendDataChannel.onerror = dataChannelError;
}

function dataChannelOpen(evt) {
    console.log("data channel is opened");
}

function dataChannelError(error) {
    console.log("Error occured on datachannel:", error);
}

// when we receive a message from the other peer, printing it on the console
function dataChannelMessage(event) {
    console.log("message:", event.data);
}

function dataChannelClose() {
    console.log("data channel is closed");
}

async function createOffer() {
    let offer = await peerConnection.createOffer({
        offerToReceiveAudio: true,
        offerToReceiveVideo: true
    });
    send({
        event: "offer",
        data: offer
    });
    await peerConnection.setLocalDescription(offer);
}

async function handleOffer(offer) {
    await peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
    let answer = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answer);
    send({
        event: "answer",
        data: answer
    });
}

async function handleCandidate(candidate) {
    await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
}

async function handleAnswer(answer) {
    await peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
    console.log("connection established successfully!!");
};

function sendMessage() {
    sendDataChannel.send(input.value);
    input.value = "";
}
