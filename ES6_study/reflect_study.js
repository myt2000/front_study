var myObject = {
    foo: 1,
    bar: 2,
    get baz() {
        return this.foo + this.bar;
    },
}

Reflect.get(myObject, 'foo') // 1
Reflect.get(myObject, 'bar') // 2
Reflect.get(myObject, 'baz') // 3

// es6用到的 let/const  析构 解构  =>
// reflect proxy 只有自己写框架才会用到



var myObject = {
    foo: 1,
    bar: 2,
    get baz() {
        return this.foo + this.bar;
    },
};

var myReceiverObject = {
    foo: 4,
    bar: 4,
};

let result = Reflect.get(myObject, 'baz', myReceiverObject)

console.log(result)

console.log(myObject)

console.log(myReceiverObject)


var myObject = {
    foo: 1,
    set bar(value) {
        return this.foo = value;
    },
}

console.log(myObject.foo) // 1

Reflect.set(myObject, 'foo', 2);
console.log(myObject.foo) // 2

Reflect.set(myObject, 'bar', 3)
console.log(myObject.foo) // 3

console.log(myObject)


var myObject = {
    foo: 4,
    set bar(value) {
        return this.foo = value;
    },
};

var myReceiverObject = {
    foo: 0,
};

Reflect.set(myObject, 'bar', 1, myReceiverObject);
console.log(myObject.foo) // 4
console.log(myReceiverObject.foo) // 1
console.log(myReceiverObject)


let p = {
    a: 'a'
};

let handler = {
    set(target, key, value, receiver) {
        console.log('set');
        Reflect.set(target, key, value, receiver)
    },
    defineProperty(target, key, attribute) {
        console.log('defineProperty');
        Reflect.defineProperty(target, key, attribute);  // 基本等同于 Object.defineProperty() 方法，唯一不同是返回 Boolean 值。
    }
};

let obj = new Proxy(p, handler);
console.log(obj)
obj.a = 'A';
console.log(obj)


// apply
const ages = [11, 33, 12, 54, 18, 96];

// 旧写法
const youngest1 = Math.min.apply(Math, ages);
const oldest1 = Math.max.apply(Math, ages);
const type1 = Object.prototype.toString.call(youngest1);
console.log(youngest1)
console.log(oldest1)
console.log(type1)

// 新写法
const youngest2 = Reflect.apply(Math.min, Math, ages);
const oldest2 = Reflect.apply(Math.max, Math, ages);
const type2 = Reflect.apply(Object.prototype.toString, youngest2, []);
console.log(youngest1)
console.log(oldest1)
console.log(type1)