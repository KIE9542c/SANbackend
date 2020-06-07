package san.sanbackend.service;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import san.sanbackend.entity.vo.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

@Service
public class UploadService {
    private static final String LRImagePath = "C:\\Users\\9\\Desktop\\SAN-master\\TestCode\\code\\LRImage\\Set5\\x";
    private static final String HRImagePath = "C:\\Users\\9\\Desktop\\SAN-master\\TestCode\\SR\\BI\\HRImage\\Set5\\x";
    private static final String SANCodePath = "C:\\Users\\9\\Desktop\\SAN-master\\TestCode\\code";
    private Logger logger = (Logger) LoggerFactory.getLogger(getClass());
    private Integer SRTime = 0;

    public synchronized Response uploadImage(int scale, MultipartFile image) throws Exception {
        //存储传来的LRImage
        String path = LRImagePath + scale;
        File imageFolder = new File(path);
        String oriImageName = image.getOriginalFilename();
        File f = new File(imageFolder, oriImageName);
        if (!f.getParentFile().exists())
            f.getParentFile().mkdirs();
        try {
            image.transferTo(f);
            logger.info("接收图片" + oriImageName + "成功");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("接收图片" + oriImageName + "失败");
            return new Response(ResponseCodeEnum.FAULT, "服务端获取图片失败，请刷因页面后重试", new ResponseUploadData(oriImageName));
        }

        //进行SR

        SRTime++;
        logger.info("SR次数:  " + SRTime);
        try {
            logger.info("SR开始");
            String command = "python main.py " +
                    "--model san " +
                    "--data_test MyImage " +
                    "--save HRImage " +
                    "--scale " + scale + " " +
                    "--n_resgroups 20 " +
                    "--n_resblocks 10 " +
                    "--n_feats 64 " +
                    "--reset " +
                    "--chop " +
                    "--save_results " +
                    "--test_only " +
                    "--testpath LRImage " +
                    "--testset Set5 " +
                    "--pre_train ../model/SAN_BI" + scale + "X.pt";
            File pythonPath = new File(SANCodePath);
            Process pr = Runtime.getRuntime().exec(command, null, pythonPath);
            BufferedReader in = new BufferedReader(new
                    InputStreamReader(pr.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            pr.waitFor();
            logger.info("SR完成");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("SR失败");
            return new Response(ResponseCodeEnum.FAULT, "SR失败，请刷新页面后重试", new ResponseUploadData(oriImageName));
        }
        //得到HRImage后删除原始的LRImage
        if (f.delete()) {
            System.out.println("delete ori image successful");
            logger.info("删除源图片文件" + oriImageName + "成功");
        }

        //使用SAN进行SR之后得到的图片格式为png
        File HRImage;
        String imageExtention = oriImageName.substring(oriImageName.lastIndexOf('.') + 1);
        if(imageExtention.equals(new String("jpg"))) {
            HRImage = new File(HRImagePath + scale + "\\" + oriImageName.subSequence(0,oriImageName.lastIndexOf('.')) + ".png");
        } else {
            HRImage = new File(HRImagePath + scale + "\\" + oriImageName);
        }

        //将得到的HRImage按照放大倍数在原来的文件名称后增加x2/x3/x4
        int pointPosition = oriImageName.lastIndexOf('.');
        String rename = new StringBuffer(oriImageName).insert(pointPosition, "x" + scale).toString();
        File renameHRImage = new File(HRImagePath + scale + "\\" + rename);

        //若已存在相同名称文件则先删除之前的文件
        if(!HRImage.exists()) {
            logger.error("SR失败,HR图像未生成");
            return new Response(ResponseCodeEnum.FAULT, "SR失败,HR图像未生成（大概率为图像过大，请使用更小的图像重新尝试）", new ResponseUploadData(oriImageName));
        }
        if(renameHRImage.exists())
            renameHRImage.delete();
        if(!HRImage.renameTo(renameHRImage)) {
            logger.error("重命名HR图像失败");
            return new Response(ResponseCodeEnum.FAULT,"重命名HR图像失败", new ResponseUploadData(oriImageName));
        }

        //若用户上传的图片格式为jpg，需要再改为jpg格式
        if(imageExtention.equals(new String("jpg"))) {
            try {
                String renameJPG = rename.substring(0, rename.lastIndexOf('.')) + ".jpg";
                File renameJPGHRImage = new File(HRImagePath + scale + "\\" + renameJPG);
                imageTransferPNGToJPG(renameHRImage, renameJPGHRImage);
                rename = renameJPG;
            }
            catch (Exception e) {
                logger.error("图片格式转换(png -> jpg)失败");
                return new Response(ResponseCodeEnum.FAULT,"图片格式转换失败", new ResponseUploadData(oriImageName));
            }
        }
        String imgURL = "http://localhost:8081/image/x" + scale + "/" + rename;
        //返回HRImage的URL
        logger.info("成功返回HR图像URL");
        return new Response(ResponseCodeEnum.SUCCESS,"SR success", new ResponseUploadData(imgURL, oriImageName));
    }

    public Response deleteImage(String name, int scale) throws Exception {
        logger.info("开始删除" + name);
        //在前端name中添加了“ SRx2”，这里要去掉
        name = name.substring(0,name.length() - 5);
        String filePath = HRImagePath + scale + "\\" + name;
        int pointPosition = filePath.lastIndexOf('.');
        filePath = new StringBuffer(filePath).insert(pointPosition, "x" + scale).toString();
        File image = new File(filePath);
        if(image.exists()){
            image.delete();
            logger.info("成功删除" + filePath);
        } else {
            logger.warn( filePath + "图像文件不存在或已删除");
        }
        return new Response(ResponseCodeEnum.SUCCESS, "delete success");
    }

    private void imageTransferPNGToJPG(File png, File jpg)throws Exception{
        FileImageInputStream fiis=new FileImageInputStream(png);
        FileImageOutputStream fios=new FileImageOutputStream(jpg);

        ImageReader jpegReader = null;
        Iterator<ImageReader> it1 = ImageIO.getImageReadersByFormatName("png");
        if(it1.hasNext())
        {
            jpegReader = it1.next();
        }
        jpegReader.setInput(fiis);

        ImageWriter bmpWriter = null;
        Iterator<ImageWriter> it2 = ImageIO.getImageWritersByFormatName("jpg");
        if(it2.hasNext())
        {
            bmpWriter = it2.next();
        }
        bmpWriter.setOutput(fios);
        BufferedImage br = jpegReader.read(0);
        bmpWriter.write(br);
        fiis.close();
        fios.close();
        logger.info("png到jpg图片转换完成");
    }

}
