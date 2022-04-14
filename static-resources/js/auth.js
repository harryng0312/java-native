class Authenticator {
    static loginByUnamePasswd(uname, passwd, callback) {
        let data = {
            username: uname,
            password: passwd
        };
        let success = function (data) {
            let result = (data.result === "0");
            if(!result) {
                alert("Login result:" + result);
            }
            callback(result);
        };
        let error = function (err) {
            alert(err);
            console.log("Error:" + err);
            callback(false);
        };
        FormUtil.postJson("login", data, success, error);
    };
    static loginByECDH(uname, passwd, callback) {
        let promise = new Promise(function (resolve, reject) {
            resolve(passwd)
        });
        promise.then(async function (val) {
            let data = DataUtil.strToBytes(val);
            const hashPasswd = await HCrypto.hash("SHA-256", data);
            console.log("Hashed passwd:" + hashPasswd);
            let sqrHashedPwd = DataUtil.bytesToBigInt(DataUtil.base64ToBytes(hashPasswd)).pow(2);
            console.log("Num passwd:" + sqrHashedPwd);
            const dhParams = {
                name: "ECDH",
                namedCurve: "P-256"
            };
            const keyPair = await HCrypto.generateKey(dhParams, ["deriveKey", "deriveBits"]);
            let priKey = keyPair.privateKey;
            let pubKey = keyPair.publicKey;
            let priKeyData = await HCrypto.exportKey("jwk", priKey);
            let pubKeyData = await HCrypto.exportKey("jwk", pubKey);
            console.log("Pri Key:" + JSON.stringify(priKeyData));
            console.log("Pub Key:" + JSON.stringify(pubKeyData));
            let commonSecretKey = await HCrypto.deriveKey({
                    name: "ECDH",
                    namedCurve: "P-256",
                    public: pubKey
                },
                priKey,
                {
                    name: "AES-CTR",
                    length: 256
                }, ["encrypt", "decrypt"]);
            let keyData = await HCrypto.exportKey("jwk", commonSecretKey);
            console.log("Common Key:" + JSON.stringify(keyData));
        }).catch(function (err) {
            alert(err);
        });
    };
    static loginByPBKDF2(uname, passwd, callback) {
        let salt = DataUtil.strToBytes("0000");
        let iterations = 10240;
        let promise = new Promise(function (resolve, reject) {
            resolve(passwd)
        });
        promise.then(async function (val) {
            let passwdBin = DataUtil.strToBytes(val);
            let key = await HCrypto.importKey("raw",
                passwdBin,
                {name: "PBKDF2"},
                ['deriveKey', 'deriveBits']);
            let webKey = await HCrypto.deriveKey({
                    name: "PBKDF2",
                    salt: salt,
                    iterations: iterations,
                    hash: "SHA-256"
                }, key,
                {name: "AES-CBC", length: 128},
                ["encrypt", "decrypt"]);
            let sKey = await HCrypto.exportKey("raw", webKey);
            console.log("Secret key:" + DataUtil.bytesToBase64(sKey));
            let counter = new Uint8Array(16);
            counter.fill(0);
            counter[counter.byteLength-1] = 1;
            let pDataBin = DataUtil.strToBytes("abcdefghijklmnopqrstuvwxyz0123456789");
            let cDataBin = await HCrypto.encrypt({
                name:"AES-CBC",
                counter: counter,
                length:128,
                iv: counter
            }, webKey, pDataBin);
            console.log("CData:" + DataUtil.bytesToBase64(cDataBin));
        }).catch(function (err) {
            console.log(err);
        });
    }
}