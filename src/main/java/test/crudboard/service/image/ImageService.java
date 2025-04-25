package test.crudboard.service.image;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import test.crudboard.domain.entity.image.Image;
import test.crudboard.domain.entity.post.Post;
import test.crudboard.repository.ImageRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<String> getImage(Long postId) {
        List<String> byPostId = imageRepository.findByPostId(postId);

        if(!byPostId.isEmpty()){
            List<String> result = new ArrayList<>();

            for (String s : byPostId) {
                // 웹에서 접근 가능한 URL 형식으로 변환
                result.add("/image/" + s);
            }

            return result;
        }

        return null;
    }


    public List<Image> save(List<String> images, Long postId){

        List<Image> list = new ArrayList<>();
        for (String url : images) {
            Image image = new Image();
            image.setUrl(url);
            image.setPost(Post.Quick(postId));
            list.add(image);
        }

        return imageRepository.saveAll(list);
    }
}
