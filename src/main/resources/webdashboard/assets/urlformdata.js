function UrlFormData(x) {

    this.dict = {};

    if (x !== undefined) {
        let parts = x.split("&");

        for (let p in parts) {
            if (parts[p].includes("=")) this.dict[(parts[p].split("=")[0])] = (parts[p].split("=")[1]);
        }
    }

    this.append = function (key, value) {
        this.dict[key] = value;
        return this;
    };

    this.get = function (key) {
        return this.dict[key];
    };

    this.toString = function () {
        let r = "";

        for (let k in this.dict) {
            r += k + "=" + this.dict[k] + "&";
        }

        if (r.length === 0) return "";

        return r.slice(0, r.length - 1);
    };
}