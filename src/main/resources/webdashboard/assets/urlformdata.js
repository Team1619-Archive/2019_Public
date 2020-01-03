//Stores, parses, and encodes data sent and received from the robot code
//Encoded format: "key=value&key=value&key=value"
function UrlFormData(x) {

    this.dict = {};

    //Decodes the url form data from a string into a js object
    if (x !== undefined) {
        let parts = x.split("&");

        for (let p in parts) {
            if (parts[p].includes("=")) {
                let [first, ...second] = parts[p].split("=");
                this.dict[first] = second.join("=");
            }
        }
    }

    //Adds a key, value pair to the js object
    this.append = function (key, value) {
        this.dict[key] = value;
        return this;
    };

    //Gets the the value assigned to the specified key in the object
    this.get = function (key) {
        return this.dict[key];
    };

    //Encodes the url form data from a js object into a string
    this.toString = function () {
        let r = "";

        for (let k in this.dict) {
            r += k + "=" + this.dict[k] + "&";
        }

        if (r.length === 0) return "";

        return r.slice(0, r.length - 1);
    };
}