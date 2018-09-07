package com.cmos.ccp.web.controller.cwpcore;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.cmos.ccp.web.controller.cwpcore.base.BaseController;
import com.cmos.core.bean.OutputObject;
import com.cmos.cwp.util.fastDFS.FastDFSFile;
import com.cmos.cwp.util.fastDFS.FileManager;

import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Decoder;
@Controller
@RequestMapping("/image")
public class ImageController extends BaseController {
	
	Logger logger = LoggerFactory.getLogger(ImageController.class);

	/**
	 * fastDFS上传文件时接收到的 按base64 
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/uploadBase64", method = RequestMethod.POST)
	@ResponseBody
	public OutputObject imgFileUpload(String imgFile) {
		OutputObject outputObject = new OutputObject();
		logger.info("imgFileUpload", "Start");
		Map<String,Object> rs = new HashMap<>();
        // 原始文件名   UEDITOR创建页面元素时的alt和title属性
        try {
        	
        	//String imgFile = inputObject.getValue("imgFile");//文件Base64
        	if(imgFile == null || imgFile.trim().length()<=0){
                outputObject.setReturnCode("0");
                outputObject.setReturnMessage("上传失败,图片为空");
         
        	}
        	if(imgFile.indexOf(',')<0){
                outputObject.setReturnCode("0");
                outputObject.setReturnMessage("上传失败,图片数据格式不正确");
         
        	}
        	String imgFileType = imgFile.substring(0, imgFile.indexOf(','));//data:image/jpg;base64,/9j/4......
        	if(imgFileType == null || imgFileType.trim().length()<=0 || imgFileType.indexOf('/')<0 || imgFileType.indexOf(';')<0){
                outputObject.setReturnCode("0");
                outputObject.setReturnMessage("上传失败,图片数据格式不正确");
         
        	}
    		String ext = "." + imgFileType.substring(imgFileType.indexOf('/') + 1 , imgFileType.indexOf(';'));
    		ext = ext.toLowerCase();
    		if(!".jpg".equals(ext) && !".png".equals(ext) && !".jpeg".equals(ext)){
                outputObject.setReturnCode("0");
                outputObject.setReturnMessage("上传失败,格式错误，只能上传格式为jpg、png、jpeg");
        
    		}
    		
    		imgFile = imgFile.substring(imgFile.indexOf(',') + 1);
    		BASE64Decoder decoder = new BASE64Decoder();
    		//Base64.Decoder decoder=Base64.getDecoder();//使用以下代码替换
    		//Base64解码  
    		byte[] b ;
				b = decoder.decodeBuffer(imgFile); 
				if(b == null){
	                outputObject.setReturnCode("0");
	                outputObject.setReturnMessage("上传失败,图片为空");
	         
				}
				if(b.length > 1024*1024 *2){
	                outputObject.setReturnCode("0");
	                outputObject.setReturnMessage("上传失败,单张图片大小不超过2M");
	         
				}
            FastDFSFile f = new FastDFSFile("" , b , ext);
            String serverPath = FileManager.upload(f);
            
            System.err.println(serverPath);
        
            rs.put("url",serverPath);         //能访问到你现在图片的路径
            outputObject.setBean(rs);
            outputObject.setReturnCode("1");
            outputObject.setReturnMessage("上传成功");

        }catch (RuntimeException e) {
        	logger.error(e.getMessage(),e);
            outputObject.setReturnCode("0");
            outputObject.setReturnMessage("上传失败,图片解码异常");
            throw e;
		} catch (Exception e) {
			logger.info("context", e);
            outputObject.setReturnCode("0");
            outputObject.setReturnMessage("文件上传失败!系统异常！");
        }
		return outputObject;
	}
}
