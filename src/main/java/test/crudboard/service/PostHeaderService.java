package test.crudboard.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import test.crudboard.domain.entity.comment.Comment;
import test.crudboard.domain.entity.post.dto.PostDetailDto;
import test.crudboard.domain.entity.post.dto.PostHeader;
import test.crudboard.domain.entity.post.dto.PostHeaderDto;
import test.crudboard.domain.error.CacheNotFoundException;
import test.crudboard.repository.JpaPostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostHeaderService {
    private final RedisService redisService;
    private final JpaPostRepository postRepository;
    private final CommentService commentService;

    /**
     * 메인화면의 타이틀 목록을 가져옴
     * @param page 가져오고자 하는 페이지
     */
    public Page<PostHeaderDto> getTitleList(Integer page, boolean isRecommend){

        Page<PostHeaderDto> dto;

        /**
         * Look aside
         * 캐시에 먼저 데이터가 있는지 확인하고 없거나 부족하다면 DB 에서 데이터를 가져옴
         * 그리고 캐시에 저장
         */
        try {
            //캐시에서 데이터를 가져옴
            dto = redisService.getTitleList(page);
        }
        catch (CacheNotFoundException e){
            log.error("[Cache miss] {}", e.getMessage());

            //페이지에 해당하는 게시글을 DB 에서 가져옴
            PageRequest created = PageRequest.of(page - 1, 20, Sort.by("id").descending());
            Page<Object[]> objectPage = postRepository.findPostList(created);

            /**
             * 가져온 게시글들의 id 를 통해 캐시의 데이터 조회
             * 조회되지 않거나, ttl 이 지난 캐시들이 있어 페이지의 모든 게시글이 캐시에 존재하지 않을 수 있다.
             */
            List<Long> list = objectPage.getContent().stream().map(o -> (Long) o[0]).toList();
            //캐시에서 게시글의 조회수 반환, 없다면 null
            Map<Long, Long> views = redisService.getView(list);

            //가져온 캐시 데이터들로 dto list 를 생성, PageImpl 에 담아 반환
            List<PostHeaderDto> dtoList = getPostHeaderDtos(objectPage,views);
            dto = new PageImpl<>(
                    dtoList,
                    objectPage.getPageable(),
                    objectPage.getTotalElements()
            );
        }

        return dto;
    }

    /**
     *
     * @param objectPage db 에서 가져온 페이지 정보들
     * @param views 캐시에서 가져온 조회수, 없다면 null
     * @return
     */
    private static List<PostHeaderDto> getPostHeaderDtos(Page<Object[]> objectPage,Map<Long, Long> views) {

        List<PostHeaderDto> dtoList = new ArrayList<>();

        //각 필드에 맞게 header 초기화
        for (Object[] content : objectPage.getContent()) {
            PostHeaderDto header = new PostHeaderDto();
            Long key = (Long)content[0];
            header.setPost_id(key);
            header.setHead((String) content[1]);
            header.setContext((String) content[2]);

            header.setView(
                    //캐시에 있는 게시글의 경우 캐시에서 조회수를 가져오고, 없다면 db 데이터를 사용
                    views.get(key) != null ? views.get(key) : (Long)content[3]
            );

            header.setCreated((LocalDateTime)content[4]);
            header.setLike_count((Long) content[5]);
            header.setComment_count((Long) content[6]);
            header.setNickname((String) content[7]);

            dtoList.add(header);
        }

        return dtoList;
    }


    /**
     * 해당하는 id의 게시글을 가져옴
     * @param postId 게시글의 id
     * @return
     */
    public PostDetailDto getPostDetailDtoById(Long postId, boolean isRecommend){
        //댓글을 먼저 가져욤
        List<Comment> footer = commentService.getCommentList(postId);
        PostHeaderDto header;

        try{
            //캐시에 데이터가 있는지 먼저 확인, 있다면 그대로 반환
            PostHeader postHeader = redisService.getPostHeader(postId);
            header = new PostHeaderDto(postHeader);
            return new PostDetailDto(header,footer);
        }
        catch (CacheNotFoundException e) {
            log.warn("[{}] {}", postId, e.getMessage());
            //캐시에 없으면 DB 에서 데이터 조회
            header = postRepository.findPostHeaderDto(postId).orElseThrow(() -> new EntityNotFoundException("entity not found"));
            //그리고 캐시에 저장
            redisService.addHeader(header);
        }


        return new PostDetailDto(header, footer);
    }
}
