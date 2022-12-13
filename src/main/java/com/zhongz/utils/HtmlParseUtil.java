package com.zhongz.utils;

import com.zhongz.entity.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {

    public static void main(String[] args) throws Exception {
        parseHtmL("万达").forEach(System.out::println);
    }

    public static List<Content> parseHtmL(String keyWords) throws Exception {
        String url = "https://search.jd.com/Search?keyword=" + keyWords;
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");
        List<Content> contents = new ArrayList<>();
        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String name = el.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setName(name);
            content.setPrice(price);
            content.setImg(img);
            contents.add(content);
        }
        return contents;
    }
}
