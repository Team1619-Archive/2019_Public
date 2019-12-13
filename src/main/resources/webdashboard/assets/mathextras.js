class Point {

    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    add(point) {
        return new Point(this.x + point.x, this.y + point.y);
    };

    subtract(point) {
        return new Point(this.x - point.x, this.y - point.y);
    }

    distance(point) {
        return Math.sqrt(Math.pow(point.x - this.x, 2) + Math.pow(point.y - this.y, 2));
    }

    toString() {
        return this.x + "," + this.y;
    }
}

class Vector extends Point {

    constructor(point1, point2) {
        super(point2.x - point1.x, point2.y - point1.y);
    }

    magnitude() {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    normalize() {
        return new Vector(new Point(0, 0), new Point((1 / this.magnitude()) * this.x, (1 / this.magnitude()) * this.y));
    }

    scale(scalar) {
        return new Vector(new Point(0, 0), new Point(this.x * scalar, this.y * scalar));
    }
}