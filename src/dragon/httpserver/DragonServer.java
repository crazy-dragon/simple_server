package dragon.httpserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class DragonServer {
	public static final String CRLF = "\r\n";
	public static final String BLANK = " ";
	private static final int BOUND = 7;
	private int port;
	
	public DragonServer(int port) {
		this.port = port;
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("The server has started...");
		new DragonServer(8888).start();
	}
	
	public void start() {
		try (ServerSocket server = new ServerSocket(port)) {
			while (true) {
				Socket client = server.accept();
				new Thread(()-> {
					try {
						// 获取输入流
						InputStream in = new BufferedInputStream(client.getInputStream());
						// 获取输出流
						OutputStream out = new BufferedOutputStream(client.getOutputStream());
						// 读取报文第一行，即请求行
						StringBuilder requestLine = new StringBuilder();
						// 我这里只读一行，不读取全部报文，程序没有报错！这是为什么？
						while (true) {
							int c = in.read();
							if (c == '\r' || c == '\r' || c == -1) break;
							// 不要直接使用 char 去读取，因为读取到结束的 -1 会转成 65535，导致后序的判断失败！
							requestLine.append((char)c);
						}
						
						// 这里有一个奇怪的问题，线程没有读取到任何数据，我这几就直接返回它了！
						if (requestLine.length() == 0) {
							return ;
						}
						String line = requestLine.toString();
						String[] lines = line.split(" ");
						System.out.println("request line: --> " + line);
						
						String method = lines[0];
						String path = lines[1];
						String protocol = lines[2];
						
						System.out.println("request method: " + method);
						System.out.println("request path: " + path);
						System.out.println("request protocol: " + protocol);
						
						// 设置一个标志变量，如果为 1，就默认为 Content-Type: plain/html，否则为其它的
						Path filepath = null;
						String contentType = null;
						String statusLine = "200 OK";
						// 路由分发
						switch (path) {
						case "/":
						case "/poem.html": 
							filepath = Paths.get("./resource", "poem.html");
							contentType = "text/html;charset=UTF-8";
							break;
						case "/no_poem.html":
							filepath = Paths.get("./resource",  "poem.html");
							contentType = "text/plain;charset=UTF-8";         // 区别在这里！浏览器不会解析该 html ！
							break;
						case "/json":
							filepath = Paths.get("./resource", "json.txt");
							contentType = "application/json;charset=UTF-8";   // 虽然它的功能和 text/plain 相似，但是表示的范围更小！
							break;
						case "/favicon.ico": 
							Random rand = new Random();
							String name = "favicon" + rand.nextInt(BOUND) + ".ico";
							filepath = Paths.get("./resource", name);
							contentType = "image/x-icon";      // 写错成了 image/x-ico 变成自动下载了！
							break;
						case "/any.jpg":
							filepath = Paths.get("./resource", "404.html");  
							contentType = "application/octet-stream";    // 这个首部非常有趣，当是对于它的介绍很少。
							break;
						default: 
							filepath = Paths.get("./resource",  "404.html");
							statusLine = "404 Not Found";
							contentType = "text/html;charset=UTF-8";         // plain/html 会自动下载了，奇怪！
							break;
						}
						
						StringBuilder headerBuilder = new StringBuilder();
						byte[] entity = Files.readAllBytes(filepath);		
						// 构造响应头
						headerBuilder.append("HTTP/1.0").append(BLANK).append(statusLine).append(CRLF)
							         .append("Server:").append(BLANK).append("dragon 1.0").append(CRLF)
						             .append("Content-Length:").append(BLANK).append(entity.length).append(CRLF)
						             .append("Content-Type:").append(BLANK).append(contentType).append(CRLF)
						             .append(CRLF);
						
						// 输出响应头
						System.out.println(headerBuilder);
						byte[] header = headerBuilder.toString().getBytes(StandardCharsets.UTF_8);
						out.write(header);
						out.write(entity);
						out.flush();   // 一定要显示刷新流，防止出错！
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (client != null) {
								client.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
