package com.studyhub.common;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Markdown 工具类
 * 把 Markdown 文本渲染为 HTML
 */
public class MarkdownUtil {

    private static final Parser parser = Parser.builder().build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

    /**
     * 将 Markdown 文本转换为 HTML
     *
     * @param markdown Markdown 格式的文本
     * @return HTML 格式的文本
     */
    public static String renderToHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}