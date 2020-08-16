# simple_server
It use java socket to create a simple http server, I think it can help you learn about http protocol's header--Content-Type.

启动方式：
1.直接启动运行
2.打包后运行，将resource文件夹和jar放入同级目录，然后执行命令： java -jar server.jar

演示：
打开浏览器，访问一下路径：
localhost:8888/                 返回主页
localhost:8888/poem.html        返回主页
localhost:8888/no_poem.html     返回不解析的html
localhost:8888/json             返回json数据
localhost:8888/favicon          返回网页的图标，多次访问，结果不同
localhost:8888/any.jpg         以二进制流的形式下载404.html，并且保存为 any.jpg
other                           返回404页


github's network is too slow, so if you want to watch more infomation, please click the link.
more info：https://blog.csdn.net/qq_40734247/article/details/108026242
