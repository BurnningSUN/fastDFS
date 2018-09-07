package com.cmos.cwp.util.fastDFS;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ServerInfo;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerGroup;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;

import com.cmos.ccp.util.PropertiesUtil;
import com.cmos.core.logger.Logger;
import com.cmos.core.logger.LoggerFactory;

public class FileManager implements FileManagerConfig {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getActionLog(FileManager.class);

	private static TrackerClient trackerClient;
	private static TrackerServer trackerServer;
	private static StorageServer storageServer;
	private static StorageClient storageClient;

	public static StorageClient concent() {
		try {
			ClientGlobal.setG_connect_timeout(60);
			ClientGlobal.setG_network_timeout(60);
			ClientGlobal.setG_charset("UTF-8");
			ClientGlobal.setG_tracker_http_port(8080);
			ClientGlobal.setG_anti_steal_token(false);
			ClientGlobal.setG_secret_key("FastDFS1234567890");
			InetSocketAddress[] trackerServers = new InetSocketAddress[1];
			trackerServers[0] = new InetSocketAddress(PropertiesUtil.getString("track_server.Address"), 22122);
			ClientGlobal.setG_tracker_group(new TrackerGroup(trackerServers));
			trackerClient = new TrackerClient();
			trackerServer = trackerClient.getConnection();
			storageClient = new StorageClient(trackerServer, storageServer);
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
		}
		return storageClient;
	}

	public static String upload(FastDFSFile file) {
	
		NameValuePair[] meta_list = new NameValuePair[3];
		meta_list[0] = new NameValuePair("width", "120");
		meta_list[1] = new NameValuePair("heigth", "120");
		meta_list[2] = new NameValuePair("author", "Diandi");

		long startTime = System.currentTimeMillis();
		String[] uploadResults = null;
		
		StorageClient storageClient = concent();
		if (StringUtils.isEmpty(storageClient)) {
			logger.info("******连接存储服务器StroageServer失败******");
		} else {
			logger.info("******连接存储服务器StroageServer成功******");
			byte[] byte1 = file.getContent();
			logger.info("*******************图片的长度为：" + byte1.length);
			String exName = file.getExt();
			logger.info("*******************图片的别名为：" + exName);
			try {
				uploadResults = storageClient.upload_file(byte1, exName, null);
			} catch (IOException e) {
				e.printStackTrace();
				logger.info("****************上传图片发生异常*********");
			} catch (MyException e) {
				e.printStackTrace();
				logger.info("****************上传图片发生异常**********");
			}
		}

		String groupName = null;
		String remoteFileName = null;

		if (uploadResults != null) {
			logger.info("**********上传图片用时:" + (System.currentTimeMillis() - startTime) + " ms");
			groupName = uploadResults[0];
			remoteFileName = uploadResults[1];
		} else {
			logger.info("**********上传图片失败***********");
		}

		String fileAbsolutePath = PropertiesUtil.getString("fastUrl") + SEPARATOR + groupName + SEPARATOR + remoteFileName;
		logger.info("upload file successfully!!!  " + "group_name: " + groupName + ", remoteFileName:" + " "
				+ remoteFileName);
		logger.info(fileAbsolutePath);

		return fileAbsolutePath;

	}

	public static FileInfo getFile(String groupName, String remoteFileName) {
		try {
			return storageClient.get_file_info(groupName, remoteFileName);
		} catch (IOException e) {
			logger.error("IO Exception: Get File from Fast DFS failed", e);
		} catch (Exception e) {
			logger.error("Non IO Exception: Get File from Fast DFS failed", e);
		}
		return null;
	}

	public static StorageServer[] getStoreStorages(String groupName) throws IOException {
		return trackerClient.getStoreStorages(trackerServer, groupName);
	}

	public static ServerInfo[] getFetchStorages(String groupName, String remoteFileName) throws IOException {
		return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
	}
}
