let a = Symbol('a')
let Object = {
    name: 'brooks',
    age: '18',
    [a]: '1111'
}
window.Object = Object


const array1 = ['a', 'b', 'c'];
const iterator = array1.keys();

for (const key of iterator) {
    console.log(key);
}

// expected output: 0
// expected output: 1
// expected output: 


