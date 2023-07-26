


var a = { a1: 1, a2: 2 }
var b = { a1: 'b', b1: 1, b2: 2, b3: 3 }
var c = { c1: 1, c2: 2 }
Object.assign(a, b, c)  // 把b的值委派到a上面

console.log(a)
// { a1: 'b', a2: 2, b1: 1, b2: 2, b3: 3 }


var a = 1
var b = a

b = 2
console.log(a)


// 伪数组
var a = {
    0: '000',
    1: '111',
    2: '222',
    length: 3
}
// 伪数组变成数组
b = Array.prototype.slice.call(a, 0)

// ES6的方法
c = Array.from(a)

console.log(c)

const e = Array.from({ length: 5 }, (v, i) => i);

console.log(e)  // [ 0, 1, 2, 3, 4 ]

const f = Array.from({ length: 5 })
console.log(f)  // [ undefined, undefined, undefined, undefined, undefined ]

