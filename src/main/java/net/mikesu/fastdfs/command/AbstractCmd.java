package net.mikesu.fastdfs.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
/**
 * <p>Description:命令抽象类 </p>
 * @author iTouch.wan
 * @date 2015年11月27日
 */
public abstract class AbstractCmd<T> implements Command<T> {
	
	protected byte requestCmd;
	protected byte responseCmd;
	protected long responseSize;
	protected byte[] body1;
	protected long body2Len = 0l;
	/**
	 * @Description: 将请求数据装入 输出socketOut
	 * @param socketOut 输出socket
	 * @throws IOException
	 * @date 2015年11月28日 下午5:38:42
	 */
	protected void request(OutputStream socketOut)throws IOException {
		socketOut.write(getRequestHeaderAndBody1());
	}
	/**
	 * @Description: 封装socket流
	 * @param socketOut 
	 * @param is 文件输入流
	 * @throws IOException   
	 * @date 2015年11月28日 下午6:37:15
	 */
	protected void request(OutputStream socketOut,InputStream is)throws IOException {
		request(socketOut);
		int readBytes;
		byte[] buff = new byte[256 * 1024];
		while ((readBytes = is.read(buff)) >= 0) {
			if (readBytes == 0) {
				continue;
			}
			/**
			 * 将is字节流装入socketOut流
			 */
			socketOut.write(buff, 0, readBytes);
		}
		is.close();
	}
	/**
	 * @Description: 获取 请求头和内容
	 * @return  字节数组
	 * @date 2015年11月28日 下午5:40:55
	 */
	protected byte[] getRequestHeaderAndBody1() {
		if(body1==null){
			body1 = new byte[0];
		}
		byte[] header = new byte[FDFS_PROTO_PKG_LEN_SIZE + 2 + body1.length];
		Arrays.fill(header, (byte) 0);
		byte[] hex_len = long2buff(body1.length+body2Len);
		System.arraycopy(hex_len, 0, header, 0, hex_len.length);
		System.arraycopy(body1, 0, header, FDFS_PROTO_PKG_LEN_SIZE + 2, body1.length);
		header[PROTO_HEADER_CMD_INDEX] = requestCmd;
		header[PROTO_HEADER_STATUS_INDEX] = (byte) 0;
		return header;
	}
	/**
	 * @Description: 将响应数据传入，封装Response对象
	 * @param socketIn	socket输入流
	 * @return
	 * @throws IOException   
	 * @date 2015年11月28日 下午5:45:00
	 */
	protected Response response(InputStream socketIn)throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int code = response(socketIn,os);
		return new Response(code, os.toByteArray());
	}
	/**
	 * @Description: 响应，将socketIn流放入os输出流
	 * @param socketIn
	 * @param os
	 * @return
	 * @throws IOException   
	 * @date 2015年11月28日 下午6:44:28
	 */
	protected int response(InputStream socketIn,OutputStream os) throws IOException {
		byte[] header = new byte[FDFS_PROTO_PKG_LEN_SIZE + 2];
		
		int bytes = socketIn.read(header);
		
		if (bytes != header.length) {
			throw new IOException("recv package size " + bytes + " != "	+ header.length);
		}

		if (header[PROTO_HEADER_CMD_INDEX] != responseCmd) {
			throw new IOException("recv cmd: " + header[PROTO_HEADER_CMD_INDEX]	+ " is not correct, expect cmd: " + responseCmd);
		}

		if (header[PROTO_HEADER_STATUS_INDEX] != SUCCESS_CODE) {
			return header[PROTO_HEADER_STATUS_INDEX];
		}

		long respSize = buff2long(header, 0);
		if (respSize < 0) {
			throw new IOException("recv body length: " + respSize + " < 0!");
		}

		if (responseSize >= 0 && respSize != responseSize) {
			throw new IOException("recv body length: " + respSize + " is not correct, expect length: " + responseSize);
		}
		

		byte[] buff = new byte[2 * 1024];
		int totalBytes = 0;
		int remainBytes = (int) respSize;

		while (totalBytes < respSize) {
			int len = remainBytes;
			if(len>buff.length){
				len = buff.length;
			}
			
			if ((bytes = socketIn.read(buff, 0, len)) < 0) {
				break;
			}
			os.write(buff, 0, bytes);
			totalBytes += bytes;
			remainBytes -= bytes;
		}

		if (totalBytes != respSize) {
			throw new IOException("recv package size " + totalBytes + " != "+ respSize);
		}
		os.close();
		return SUCCESS_CODE;
	}

    protected String metaDataToStr(Map<String,String> metaData){
        StringBuffer sb = new StringBuffer();
        for(String key:metaData.keySet()){
            sb.append(FDFS_RECORD_SEPERATOR);
            sb.append(key);
            sb.append(FDFS_FIELD_SEPERATOR);
            sb.append(metaData.get(key));
        }

        return sb.toString().substring(FDFS_RECORD_SEPERATOR.length());
    }
	
	public static byte[] long2buff(long n) {
		byte[] bs;

		bs = new byte[8];
		bs[0] = (byte) ((n >> 56) & 0xFF);
		bs[1] = (byte) ((n >> 48) & 0xFF);
		bs[2] = (byte) ((n >> 40) & 0xFF);
		bs[3] = (byte) ((n >> 32) & 0xFF);
		bs[4] = (byte) ((n >> 24) & 0xFF);
		bs[5] = (byte) ((n >> 16) & 0xFF);
		bs[6] = (byte) ((n >> 8) & 0xFF);
		bs[7] = (byte) (n & 0xFF);

		return bs;
	}
	
	public static long buff2long(byte[] bs, int offset) {
		return (((long) (bs[offset] >= 0 ? bs[offset] : 256 + bs[offset])) << 56)
				| (((long) (bs[offset + 1] >= 0 ? bs[offset + 1]
						: 256 + bs[offset + 1])) << 48)
				| (((long) (bs[offset + 2] >= 0 ? bs[offset + 2]
						: 256 + bs[offset + 2])) << 40)
				| (((long) (bs[offset + 3] >= 0 ? bs[offset + 3]
						: 256 + bs[offset + 3])) << 32)
				| (((long) (bs[offset + 4] >= 0 ? bs[offset + 4]
						: 256 + bs[offset + 4])) << 24)
				| (((long) (bs[offset + 5] >= 0 ? bs[offset + 5]
						: 256 + bs[offset + 5])) << 16)
				| (((long) (bs[offset + 6] >= 0 ? bs[offset + 6]
						: 256 + bs[offset + 6])) << 8)
				| ((long) (bs[offset + 7] >= 0 ? bs[offset + 7]
						: 256 + bs[offset + 7]));
	}

    /**
     * 传入带group的fileid,返回group和filename的二元数组
     * @param file_id fileid like g1/M00/00/00/abc.jpg
     * @return string[2] string[0] = g1, string[1]=M00/00/00/abc.jpg
     */
    public static String[] splitFileId(String file_id) {
        int pos = file_id.indexOf("/");
        if ((pos <= 0) || (pos == file_id.length() - 1)) {
            return null;
        }

        String[] results = new String[2];
        results[0] = file_id.substring(0, pos); //group name
        results[1] = file_id.substring(pos + 1); //file name
        return results;
    }
	
	protected class Response {
		
		private int code;
		/**
		 * 返回数据,字节数组
		 */
		private byte[] data;
		
		public Response(int code) {
			super();
			this.code = code;
		}
		public Response(int code, byte[] data) {
			super();
			this.code = code;
			this.data = data;
		}
		public boolean isSuccess(){
			return code == SUCCESS_CODE;
		}
		public int getCode() {
			return code;
		}
		public void setCode(int code) {
			this.code = code;
		}
		public byte[] getData() {
			return data;
		}
		public void setData(byte[] data) {
			this.data = data;
		}
		
	}
	
}
