package com.sweetscoop.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sweetscoop.admin.dto.request.NoticeRequest;
import com.sweetscoop.admin.dto.response.NoticeResponse;
import com.sweetscoop.admin.entity.HqManager;
import com.sweetscoop.admin.entity.Notice;
import com.sweetscoop.admin.repository.HqManagerRepository;
import com.sweetscoop.admin.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {


    private final NoticeRepository noticeRepository;

    private final HqManagerRepository hqManagerRepository;



    // 등록
    public Notice save(NoticeRequest request){

        HqManager hqManager =
                hqManagerRepository.findByLoginId(request.getHqManagerId())
                .orElseThrow(
                    () -> new RuntimeException("본사 관리자가 없습니다.")
                );


        Notice notice = Notice.builder()
        	    .title(request.getTitle())
        	    .content(request.getContent())
        	    .hqManager(hqManager)
        	    .createdAt(LocalDateTime.now())
        	    .build();


        return noticeRepository.save(notice);

    }



    // 전체 조회
    public List<NoticeResponse> findAll(){


    	return noticeRepository.findAllByOrderByCreatedAtDesc()
    	        .stream()
    	        .map(notice -> NoticeResponse.builder()
    	                .id(notice.getId())
    	                .hqManagerId(notice.getHqManager().getId())
    	                .hqManagerName(notice.getHqManager().getName())
    	                .title(notice.getTitle())
    	                .content(notice.getContent())
    	                .createdAt(notice.getCreatedAt())
    	                .updateAt(notice.getUpdateAt())
    	                .build()
    	        )
    	        .toList();

    }


    // 상세 조회
    public NoticeResponse findById(Integer id){


        Notice notice =
                noticeRepository.findById(id)
                .orElseThrow(
                    () -> new RuntimeException("공지사항 없음")
                );


        return convert(notice);

    }



    // 수정
    public NoticeResponse update(
            Integer id,
            NoticeRequest dto
    ){

        Notice notice =
                noticeRepository.findById(id)
                .orElseThrow();


        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setUpdateAt(LocalDateTime.now());


        return convert(noticeRepository.save(notice));

    }




    // 삭제
    public void delete(Integer id){

        noticeRepository.deleteById(id);

    }



    private NoticeResponse convert(
            Notice notice
    ){

        return NoticeResponse.builder()

                .id(notice.getId())

                .hqManagerId(
                    notice.getHqManager().getId()
                )

                .title(notice.getTitle())

                .content(notice.getContent())

                .createdAt(
                    notice.getCreatedAt()
                )

                .updateAt(
                    notice.getUpdateAt()
                )

                .build();

    }

}