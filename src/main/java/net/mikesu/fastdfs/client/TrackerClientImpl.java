package net.mikesu.fastdfs.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import net.mikesu.fastdfs.FastdfsClientConfig;
import net.mikesu.fastdfs.command.CloseCmd;
import net.mikesu.fastdfs.command.Command;
import net.mikesu.fastdfs.command.GroupInfoCmd;
import net.mikesu.fastdfs.command.QueryDownloadCmd;
import net.mikesu.fastdfs.command.QueryUpdateCmd;
import net.mikesu.fastdfs.command.QueryUploadCmd;
import net.mikesu.fastdfs.command.StorageInfoCmd;
import net.mikesu.fastdfs.data.GroupInfo;
import net.mikesu.fastdfs.data.Result;
import net.mikesu.fastdfs.data.StorageInfo;
import net.mikesu.fastdfs.data.UploadStorage;
/**
 * Tracker客户端实现类
 * @author iTouch.wan
 * @date 2015年11月27日
 */
public class TrackerClientImpl implements TrackerClient{
	
	private Socket socket;
	private String host;
	private Integer port;
	private Integer connectTimeout = FastdfsClientConfig.DEFAULT_CONNECT_TIMEOUT * 1000;
	
	private Integer networkTimeout = FastdfsClientConfig.DEFAULT_NETWORK_TIMEOUT * 1000;
	
	public TrackerClientImpl(String address){
		super();
		String[] hostport = address.split(":");
		this.host = hostport[0];
		this.port = Integer.valueOf(hostport[1]);
	}
	
	public TrackerClientImpl(String address,Integer connectTimeout, Integer networkTimeout){
		this(address);
		this.connectTimeout = connectTimeout;
		this.networkTimeout = networkTimeout;
	}
	/**
	 * @Description: 获取socket
	 * @return
	 * @throws IOException   
	 * @date 2015年11月28日 下午5:16:39
	 */
	private Socket getSocket() throws IOException{
		if(socket==null){
			socket = new Socket();
			//表示如果对方连接状态networkTimeout毫秒没有收到数据的话强制断开客户端。
			socket.setSoTimeout(networkTimeout);
			//当客户端的Socket构造方法请求与服务器连接时，可能要等待一段时间。
			//默认情况下，Socket构造方法会一直等下去，直到连接成功，或者出现异常。
			//Socket构造方法请求连接时，受底层网络的传输速度的影响，可能会处于长时间的等待状态。
			//如果希望设定等待连接的时间,传入一个参数。在这个时间内连接不上，那么会抛出SocketTimeoutException。
			socket.connect(new InetSocketAddress(host, port),connectTimeout);
		}
		return socket;
	}
	

	public void close() throws IOException{
		Socket socket = getSocket();
		Command<Boolean> command = new CloseCmd();
		command.exec(socket);
		socket.close();
		socket = null;
	}

	
	public Result<UploadStorage> getUploadStorage() throws IOException{
		Socket socket = getSocket();
		Command<UploadStorage> command = new QueryUploadCmd();
		return command.exec(socket);
	}
	
	public Result<String> getUpdateStorageAddr(String group,String fileName) throws IOException{
		Socket socket = getSocket();
		Command<String> cmd = new QueryUpdateCmd(group,fileName);
		return cmd.exec(socket);
	}
	
	public Result<String> getDownloadStorageAddr(String group,String fileName) throws IOException{
		Socket socket = getSocket();
		Command<String> cmd = new QueryDownloadCmd(group,fileName);
		return cmd.exec(socket);
	}
	
	public Result<List<GroupInfo>> getGroupInfos() throws IOException{
		Socket socket = getSocket();
		Command<List<GroupInfo>> cmd = new GroupInfoCmd();
		return cmd.exec(socket);
	}
	
	public Result<List<StorageInfo>> getStorageInfos(String group) throws IOException{
		Socket socket = getSocket();
		Command<List<StorageInfo>> cmd = new StorageInfoCmd(group);
		return cmd.exec(socket);
	}
	
}
