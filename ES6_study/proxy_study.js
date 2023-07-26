let beProxy = {}

let proxy = new Proxy(beProxy, {
    get: function (target, key) {
        console.log(`get target: ${target}`)
        // console.log(`get key: ${key}`)
        console.log('get key: ')
        console.log(key)
        return Reflect.get(target, key)
    },
    set: function (target, key, value) {
        console.log(`set target: ${target}`)
        console.log(`set key: ${key}`)
        console.log(`set value: ${value}`)
        return Reflect.set(target, key, value)
    }
})

proxy.name = 'brooks'
// console.log(`beProxy: ${beProxy}`)
console.log(`proxy: ${proxy}`)


let game = {
    lives: 3
}

let proxy1 = new Proxy(game, {
    get(target, name) {
        return Reflect.get(target, name)
    },
    set(target, name, value) {
        if (name === 'lives' && value < 0) {
            value = 0
        }
        return Reflect.set(target, name, value)
    }
})

proxy1.lives = 4
console.log(proxy1.lives) // 4

proxy1.lives = -1
console.log(proxy1.lives)  // 0 


var person1 = {
    name: "张三"
};

var proxy2 = new Proxy(person1, {
    get: function (target, propKey) {
        if (propKey in target) {
            return target[propKey];
        } else {
            throw new ReferenceError("Prop name \"" + propKey + "\" does not exist.");
        }
    }
});

console.log(proxy2.name) // "张三"
// proxy2.age // 抛出一个错误


console.log(Number(1))