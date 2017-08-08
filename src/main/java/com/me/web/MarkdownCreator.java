package com.me.web;

import org.commonmark.Extension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class MarkdownCreator {
    @EventListener
    public void processMarkdown(ContextRefreshedEvent event) throws IOException{
        List<Extension> extensions = Arrays.asList(YamlFrontMatterExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        Path postsPath = FileSystems.getDefault().getPath("posts");
        List<String> names = new ArrayList<>();
        try(Stream<Path> paths = Files.list(postsPath)) {
            paths.forEach(path -> {
                String markdownFileName=path.getFileName().toString().split("\\.")[0];
                Path markdownPath = FileSystems.getDefault().getPath("static",markdownFileName+".html");
                try(Reader r = Files.newBufferedReader(path);
                    BufferedWriter w = Files.newBufferedWriter(markdownPath)) {
                    Node node = parser.parseReader(r);
                    YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();
                    node.accept(visitor);
                    Map<String, List<String>> data = visitor.getData();
                    names.add(data.getOrDefault("name", Arrays.asList("No name")).get(0));
                    new HtmlRenderer.Builder().build().render(node, w);
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                }
            });
        }
        System.out.println(names.toString());
    }
}
