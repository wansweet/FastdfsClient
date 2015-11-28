package net.mikesu.fastdfs;

import java.io.File;
import java.util.Map;
/**
 * <p>Description: 分布式客户端接口</p>
 * @author iTouch.wan
 * @date 2015年11月27日
 */
public interface FastdfsClient {
	/**
	 * @Description: 上传文件
	 * @param file 要上传的文件
	 * @return	file_url(group name and filename)
	 * @throws Exception   
	 * @date 2015年11月27日 下午9:41:02
	 */
	public String upload(File file) throws Exception;
	/**
	 * @Description: 上传文件
	 * @param file	要上传的文件
	 * @param fileName	文件名
	 * @return file_url(group name and filename)
	 * @throws Exception   
	 * @date 2015年11月27日 下午10:25:18
	 */
	public String upload(File file,String fileName) throws Exception;
	public String getUrl(String fileId) throws Exception;
	public Boolean setMeta(String fileId,Map<String,String> meta) throws Exception;
	public Map<String,String> getMeta(String fileId) throws Exception;
	public Boolean delete(String fileId) throws Exception;
	public void close();


    /**
     * 上传一个文件
     * @param file 要上传的文件
     * @param ext 文件扩展名
     * @param meta meta key/value的meta data，可为null
     * @return fileid 带group的fileid
     * @throws Exception
     */
    public String upload(File file,String ext,Map<String,String> meta) throws Exception;

    /**
     * upload slave
     * @param file
     * @param fileid 带group的fileid,like group1/M00/00/01/abc.jpg
     * @param prefix slave的扩展名，如200x200
     * @param ext 文件扩展名，like jpg，不带.
     * @return 上传后的fileid   group1/M00/00/01/abc_200x200.jpg
     * @throws Exception
     */
    public String uploadSlave(File file,String fileid, String prefix, String ext) throws Exception;

}
