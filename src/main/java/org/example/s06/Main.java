package org.example.s06;

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String url = "https://60s.viki.moe/";
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static String get(String url) {
        String result = null;
        try {
            Response response = Request.get(url).execute();
            result = response.returnContent().asString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        String content = get(url);
        if (content == null) {
            log.error("获取数据失败！");
            return;
        }
        Result result = GsonUtil.json2Bean(content, Result.class);
        if (result.getStatus() != 200) {
            log.error(result.getMessage());
            return;
        }
        log.info(result.getMessage());
        String head = null;
        String foot = null;
        List<String> body = new ArrayList<>();
        int length = result.getData().size();
        if (length == 0) {
            return;
        }
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                head = result.getData().get(i);
            } else if (i == length - 1) {
                foot = result.getData().get(i);
            } else {
                body.add(result.getData().get(i));
            }
        }
        String templateHtml = new String(Files.readAllBytes(Paths.get("template.html")), StandardCharsets.UTF_8);
        templateHtml = templateHtml.replace("${head}", "<h2>" + head + "</h2>")
                .replace("${foot}", "<p>" + foot + "</p>")
                .replace("${date}", String.valueOf(System.currentTimeMillis()));
        StringBuilder sb = new StringBuilder();
        for (String s : body) {
            sb.append("<p>").append(s).append("</p>");
        }
        templateHtml = templateHtml.replace("${content}", sb.toString());
        // index.html
        Path indexPath = Paths.get("index.html");
        Files.deleteIfExists(indexPath);
        Files.createFile(indexPath);
        Files.write(indexPath, templateHtml.getBytes(StandardCharsets.UTF_8));
        // README.md
        Path readmePath = Paths.get("README.md");
        Files.deleteIfExists(readmePath);
        Files.createFile(readmePath);
        sb = new StringBuilder("# " + head + "\n\n");
        for (String s : body) {
            sb.append(s).append("\n\n");
        }
        sb.append("\n\n").append(foot).append("\n\n");
        Files.write(readmePath, sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
