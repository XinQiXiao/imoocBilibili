package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.VideoDao;
import com.imooc.bilibili.domain.*;
import com.imooc.bilibili.domain.constant.UserMomentsConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Value("${fdfs.http.storage-addr}")
    private String fastdfsUrl;

    @Transactional
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(new Date());
        videoDao.addVideos(video);
        //保存视频标签
        Long videoId = video.getId();
        List<VideoTag> tagList = video.getVideoTagList();
        tagList.forEach(item -> {
            item.setCreateTime(now);
            item.setVideoId(videoId);
        });
        videoDao.batchAddVideoTags(tagList);

    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no-1)*size);
        params.put("limit", size);
        params.put("area" , area);
        List<Video> list = new ArrayList<>();
        Integer total = videoDao.pageCountVideos(params);
        if(total > 0){
            list = videoDao.pageListVideos(params);

        }
        return new PageResult<>(total, list);
    }

    public List<Video> getVideoCount(List<Video> videoList){
        if(!videoList.isEmpty()){
            //获取视频id集合
            Set<Long> videoIdSet = videoList.stream().map(Video :: getId)
                    .collect(Collectors.toSet());
        }
        return videoList;
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) {
        try{
            fastDFSUtil.viewVideoOnlineBySlices(request, response, url);
        }catch (Exception ignored){}
    }

    public void addVideoLike(Long videoId, Long userId) {
        Video video = videoDao.getVideoById(videoId);
        if(video == null){
            throw new ConditionException("非法视频！");
        }
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        if(videoLike != null){
            throw new ConditionException("已经赞过！");
        }
        videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId, userId);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count = videoDao.getVideoLikes(videoId);
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        boolean like = videoLike != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }
}
