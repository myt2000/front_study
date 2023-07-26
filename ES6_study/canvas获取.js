var canvas = document.getElementById("canvas");

// ctx = canvas.getContext("2d");
// ctx.beginPath();
// ctx.moveTo(d / 2, 0);
// ctx.lineTo(d, d);
// ctx.lineTo(0, d);
// ctx.closePath();
// ctx.fillStyle = "yellow";
// ctx.fill();

var canvas = document.getElementById("canvas");


canvas.toBlob(function (blob) {
    var newImg = document.createElement("img"),
        url = URL.createObjectURL(blob);

    newImg.onload = function () {
        // no longer need to read the blob so it's revoked
        URL.revokeObjectURL(url);
    };

    newImg.src = url;
    document.body.appendChild(newImg);
});


let btn = document.getElementById('btn')
btn.onclick = () => {
    let img = document.getElementById('img')
    console.log('img', img);
    const downUrl = img.src //获取图片的路径
    img.crossOrigin = 'anonymous'
    img.onload = function () {
        let canvas = document.createElement('canvas') //创建canvas
        canvas.width = img.width
        canvas.height = img.height
        let context = canvas.getContext('2d') //getContext() 方法返回一个用于在画布上绘图的环境
        context.drawImage(img, 10, 10, img.width, img.height) //drawImage() 方法在画布上绘制图像、画布或视频
        let url = canvas.toDataURL('image/png') //方法返回一个包含图片展示的 data URI base64。
        let a = document.createElement('a')
        a.download = '画画的女孩'
        a.href = url
        let event = new MouseEvent('click') //创建鼠标单击事件

        a.dispatchEvent(event) //触发自定义单击事件
    }
    img.src = downUrl
}



