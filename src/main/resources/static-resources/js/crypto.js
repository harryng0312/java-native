const HCrypto = {
    crypto: window.crypto ? window.crypto : window.msCrypto ? window.msCrypto : null,
    subtle: window.crypto.subtle ? window.crypto.subtle : window.msCrypto ? window.msCrypto.subtle : null,
    // SHA-256
    hash: function (algName, data) {
        const promise = this.subtle.digest({name: algName}, data);
        return promise.then(function (value) {
            return DataUtil.bytesToBase64(value);
        });
    },
    // HMAC RSA-PSS ECDSA
    sign: function (param, key, data) {
        const keyBin = DataUtil.base64ToBytes(key);
        const promise = this.subtle.sign(param, keyBin, data);
        return promise.then(function (value) {
            return DataUtil.bytesToBase64(value);
        }).catch(function (err) {
            console.error(err);
        });
    },
    verify: function (param, key, signData, data) {
        const keyBin = DataUtil.base64ToBytes(key);
        const promise = this.subtle.verify(param, keyBin, signData, data);
        return promise.then(function (value) {
            return DataUtil.bytesToBase64(value);
        }).catch(function (err) {
            console.error(err);
        });
    },

    // key functions
    generateKey: function (param, keyUsages) {
        const keyPair = this.subtle.generateKey(param, true, keyUsages);
        return keyPair;
    },
    exportKey: function (type, key) {
        const exportedKey = this.subtle.exportKey(type, key);
        return exportedKey;
    },
    importKey: function (type, keyData, alg, keyUsages) {
        const importedKey = this.subtle.importKey(type, keyData, alg, false, keyUsages);
        return importedKey;
    },
    deriveKey: function (param, baseKey, derivedKeyType, keyUsages) {
        const deriveKey = this.subtle.deriveKey(param, baseKey, derivedKeyType, true, keyUsages);
        return deriveKey;
    },
    encrypt: function (param, key, data) {
        const encryptor = this.subtle.encrypt(param, key, data);
        return encryptor;
    },
    decrypt: function (param, key, data) {
        const decryptor = this.subtle.decrypt(param, key, data);
        return decryptor;
    }
};
