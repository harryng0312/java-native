let GTextEncoder = new TextEncoder();
let GTextDecoder = new TextDecoder("utf-8");
class DataUtil{

    static byteArrayToBase64(bytes){
        let binary = "";
        let len = bytes.byteLength;
        for (let i = 0; i < len; i++) {
            binary += String.fromCharCode( bytes[i] );
        }
        return window.btoa( binary );
    };

    static base64ToByteArray(base64) {
        let binary_string = window.atob(base64);
        let len = binary_string.length;
        let bytes = new Uint8Array(len);
        for (let i = 0; i < len; i++) {
            bytes[i] = binary_string.charCodeAt(i);
        }
        return bytes.buffer;
    }

    static bytesToBase64 (buffer) {
        let b64 = btoa(String.fromCharCode.apply(null, new Uint8Array(buffer)));
        return b64;
    };
    static base64ToBytes (b64) {
        let binStr = atob(b64);
        let rawLength = binStr.length;
        let array = new Uint8Array(new ArrayBuffer(rawLength));
        for (let i = 0; i < rawLength; i++) {
            array[i] = binStr.charCodeAt(i);
        }
        return array;
    };

    static strToBytes (str) {
        let bytes = GTextEncoder.encode(str);
        return bytes;
    };
    static bytesToStr (buffer) {
        let str = GTextDecoder.decode(buffer);
        return str;
    };

    static bigIntToBytes (bn) {
        if (bn != null && bn !== undefined) {
            let hex = bn.toString(16);
            if (hex.length % 2) {
                hex = '0' + hex;
            }
            let len = hex.length / 2;
            let u8 = new Uint8Array(len);
            let i = 0;
            let j = 0;
            while (i < len) {
                u8[i] = parseInt(hex.slice(j, j + 2), 16);
                i += 1;
                j += 2;
            }
            return u8;
        }
    };
    static bytesToBigInt (buf) {
        let hex = [];
        let u8 = Uint8Array.from(buf);
        u8.forEach(function (i) {
            let h = i.toString(16);
            if (h.length % 2) {
                h = '0' + h;
            }
            hex.push(h);
        });
        return bigInt(hex.join(''), 16);
    }
};
class FormUtil {
    static postJson (url, data, success, error) {
        $.ajax({
            url: url,
            crossDomain: false,
            dataType: "json",
            headers: {
                'accept': 'application/json',
                'Content-Type': 'application/json'
            },
            method: "POST",
            scriptCharset: "utf-8",
            processData: false,
            data: JSON.stringify(data),
            success: success,
            error: error,
        });
    }
}
class PubSubUtil {
    subscribers = {};
    publish(event, data) {
        if (!this.subscribers[event]) return;
        this.subscribers[event].forEach(cb => cb(data));
    };
    subscribe(event, callback) {
        if (!this.subscribers[event]) {
            this.subscribers[event] = [];
        }
        this.subscribers[event].push(callback);
    };
    unsubscribe(event, callback) {
        if (this.subscribers[event]) {
            if (callback) {
                this.subscribers[event].filter(cb => cb === callback).clear()
            } else {
                this.subscribers[event].clear()
            }
            if (!this.subscribers[event]) {
                this.subscribers[event].remove()
            }
        }
    }
}
class FileReaderUtilParams {
    fileUploadUtil = null;
    file = null;
    uploadCallback = (dataBuff) => {};
    uploadStartCallback = (e) => {};
    uploadEndCallback = (e) => {};
    uploadDoneCallback = (e) => {};
    abortCallback = (e) => {};
    errorCallback = (e) => {this.fileUploadUtil.reset();};
}
class FileReaderUtil {
    static DEFAULT_BUFFER_SIZE = 256 * 1024;
    file = null;
    offset = 0;
    fileSize = 0;
    fileName = "";
    fileType = undefined;
    fileReader = null;

    constructor(params){
        params.fileUploadUtil = this;
        this.fileReader = new FileReader();
        this.file = params.file;
        this.fileSize = this.file.size;
        this.fileName = this.file.name;
        this.fileType = this.file.type;

        this.uploadCallback = params.uploadCallback;
        this.uploadStartCallback = params.uploadStartCallback;
        this.uploadEndCallback = params.uploadEndCallback;
        this.errorCallback = params.errorCallback;
        this.abortCallback = params.abortCallback;
        this.uploadDoneCallback = params.uploadDoneCallback;

        this.reset();
    }

    get readBufferSize(){
        return FileReaderUtil.DEFAULT_BUFFER_SIZE;
    }

    get hasBlock(){
        return this.offset < this.fileSize;
    }

    seek(){
        let offsetEnd = Math.min(this.offset + this.readBufferSize, this.fileSize);
        if(this.hasBlock){
            this.fileReader.readAsArrayBuffer(this.file.slice(this.offset, offsetEnd));
            console.log(`read chunk from ${this.offset} to ${offsetEnd}`);
            this.offset = offsetEnd;
        }else{
            this.uploadDoneCallback(this);
        }
    }

    reset(){
        this.offset = 0;
        if(this.file==null) return;
        this.fileSize = this.file.size;
        this.fileName = this.file.name;
        this.fileType = this.file.type;

        this.fileReader.onload = (e) => {
            this.uploadCallback(e.target.result);
        };
        this.fileReader.onerror = (e) => {
            this.errorCallback(e);
        };
        this.fileReader.onloadstart = (e) => {
            this.uploadStartCallback(e);
        };
        this.fileReader.onloadend = (e) => {
            this.uploadEndCallback(e);
        }
        this.fileReader.onabort = (e) => {
            this.abortCallback(e);
        };
    }

    start(){
        this.seek();
    }
    abort(){
        this.fileReader.abort();
    }
}
