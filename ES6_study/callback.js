readFile('C:\\1.txt', function (error, data) {
    if (error) {
        console.log('成功')
        console.log(data.toString())
    } else {
        console.log('读取文件失败')
    }
})


// jqeury的使用方法
$.ajax({
    url: '2.txt',
    success: function (response) {
        console.log('成功')
    },
    error: function () {
        console.log('失败')
    }
})

readFilePromise('C:\\1.txt')
    .then(function () { }, function () { })  // 第一个参数表示成功，第二个参数表示失败
    .then(function () { }, function () { })
    .then(function () { }, function () { })


function 获取用户信息() {
    return new Promise(function (resolve, reject) {
        console.log("第一次获取用户信息")
        resolve('姓名方方')
    })
}

function 打印用户信息(用户信息) {
    return new Promise(function (resolve, reject) {
        console.log(用户信息)
        resolve()
    })
}

function 获取另一个用户信息() {
    return new Promise(function (resolve, reject) {
        console.log("第二次用户用户信息")
        resolve('姓名小白')
    })
}


获取用户信息()
    .then(打印用户信息)
    .then(获取另一个用户信息)
    .then(打印用户信息)




function 获取用户信息(name) {
    return new Promise(function (resolve, reject) {
        if (name === '方方') {
            console.log("我认识方方")
            resolve('方方是一个胖子')
        } else {
            console.log('不认识')
            reject()
        }
    })
}

function 获取好友用户信息(name) {
    return new Promise(function (resolve, reject) {
        if (name === '方方') {

            resolve('张三、李四、王五')
        } else {
            console.log('不认识')
            reject()
        }
    })
}


function 打印信息(data) {
    return new Promise(function (resolve, reject) {
        console.log(data)
        resolve()
    })
}

let 用户信息 = await 获取用户信息('方方')
console.log(用户信息)

获取用户信息('方方')
    .then(打印信息)
    .then(获取好友用户信息)
    .then(打印信息)

获取用户信息('方方')
    .catch(function () { })
    .finally(f1)
    .then(
        function (d) { console.log(d) },
        function () { console.log("看来它不认识方方") }
    )



