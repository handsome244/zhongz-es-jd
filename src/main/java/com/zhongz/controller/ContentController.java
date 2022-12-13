package com.zhongz.controller;

import com.zhongz.servrice.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;


    /**
     * 爬虫爬取信息并保存数据到elasticsearch服务器
     * @param keywords
     * @return
     * @throws Exception
     */
    @GetMapping("/parse/{keywords}")
    public Boolean index(@PathVariable("keywords") String keywords) throws Exception {
        return contentService.parseContent(keywords);
    }

    /**
     * 查询并分页
     * @param keywords
     * @param pageNo
     * @param size
     * @return
     * @throws Exception
     */
    @GetMapping("/search/{keywords}/{pageNo}/{size}")
    public List<Map<String, Object>> search(@PathVariable("keywords") String keywords,
                                            @PathVariable("pageNo") int pageNo,
                                            @PathVariable("size") int size) throws Exception {
        return contentService.searchHighlighter(keywords, pageNo, size);
    }
}
