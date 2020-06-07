package san.sanbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import san.sanbackend.entity.vo.Response;
import san.sanbackend.service.UploadService;

import javax.validation.constraints.Min;

@Validated
@RestController
public class UploadController {
    private UploadService uploadService;

    @Autowired
    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @CrossOrigin
    @PostMapping("upload")
    public Response uploadImage(
            @Min(value = 2, message = "放大倍率错误") @RequestParam int scale,
            MultipartFile file
    ) throws Exception {
        return uploadService.uploadImage(scale, file);
    }

    @CrossOrigin
    @PostMapping("delete")
    public Response deleteImage(
            String name,
            @Min(value = 2, message = "放大倍率错误") @RequestParam int scale
    ) throws Exception {
        return uploadService.deleteImage(name,scale);
    }

}
