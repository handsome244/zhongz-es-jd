package com.zhongz.servrice;

import com.alibaba.fastjson.JSON;
import com.zhongz.entity.Content;
import com.zhongz.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 解析京东页面并保存到ES中
     * @param keyWords
     * @return
     * @throws Exception
     */
    public boolean parseContent(String keyWords) throws Exception {
        List<Content> contents = HtmlParseUtil.parseHtmL(keyWords);

        // 插入es
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_goods")
            .id("" + (i + 1))
            .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    /**
     * 查询接口
     * @param keywords
     * @param pageNo
     * @param size
     * @return
     */
    public List<Map<String, Object>> search(String keywords, int pageNo, int size) throws IOException {
        if(pageNo <= 1){
            pageNo = 1;
        }
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(pageNo);
        sourceBuilder.size(size);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", keywords);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        List<Map<String, Object>> maps = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            maps.add(hit.getSourceAsMap());
        }

        return maps;
    }

    /**
     * 高亮设置
     * @param keywords
     * @param pageNo
     * @param size
     * @return
     */
    public List<Map<String, Object>> searchHighlighter(String keywords, int pageNo, int size) throws IOException {
        if(pageNo <= 1){
            pageNo = 1;
        }
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(pageNo);
        sourceBuilder.size(size);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", keywords);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        List<Map<String, Object>> maps = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            // 解析高亮的字段, 并替换原有的字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            if(name != null){
                Text[] fragments = name.fragments();
                StringBuilder new_name = new StringBuilder();
                for (Text fragment : fragments) {
                    new_name.append(fragment);
                }
                sourceAsMap.put("name", new_name.toString());
            }
            maps.add(sourceAsMap);
        }

        return maps;
    }
}
