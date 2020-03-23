package org.tallison.cord19;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JsonToTxt {

    static Pattern NON_ASCII_REMOVER = Pattern.compile("(?i)[^-_:/+()'\"\\[\\]a-z0-9,.\r\n]+");

    public static void main(String[] args) throws Exception {
        File input = new File("/Users/allison/Downloads/kaggle");
        Path output = Paths.get("/Users/allison/Downloads/kaggle_txt");
        for (File dir : input.listFiles()) {
            if (dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    process(f, new File(output + "/" + dir.getName()).toPath());

                }
            }
        }
    }

    private static void process(File f, Path outputDir) throws Exception {
        if (! Files.isDirectory(outputDir)) {
            Files.createDirectories(outputDir);
        }
        if (f.getName().startsWith(".")) {
            return;
        }
        System.out.println(f.getAbsolutePath());
        JsonElement root = null;
        try (Reader reader = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader);
        }
        List<String> lines = new ArrayList<>();
        for (String k : new String[]{"abstract", "body_text"}) {
            JsonArray arr = root.getAsJsonObject().get(k).getAsJsonArray();
            for (JsonElement item : arr) {
                String txt = item.getAsJsonObject().get("text").getAsString();
                //txt = NON_ASCII_REMOVER.matcher(txt).replaceAll(" ");
                lines.add(txt + "\n");
            }
            lines.add("\n");
        }
        Path target = outputDir.resolve(f.getName() + ".txt");
        Files.write(target, lines, StandardCharsets.UTF_8);
    }
}
