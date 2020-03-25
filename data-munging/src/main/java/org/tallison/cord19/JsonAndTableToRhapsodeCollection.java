package org.tallison.cord19;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.serialization.JsonMetadataList;
import org.apache.tika.sax.AbstractRecursiveParserWrapperHandler;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonAndTableToRhapsodeCollection {

    int missingSha = 0;
    String missingShaPrefix = "missing_content_";
    public static void main(String[] args) throws Exception {
        Path kaggle = Paths.get(args[0]);
        Path rhapsodeCollection = Paths.get(args[1]);
        JsonAndTableToRhapsodeCollection converter = new JsonAndTableToRhapsodeCollection();
        converter.execute(kaggle, rhapsodeCollection);
    }

    private void execute(Path kaggle, Path rhapsodeCollection) throws Exception {
        Map<String, PubMetadata> map = loadMetadata(kaggle);
        dumpMissingContent(map, rhapsodeCollection);
        dumpContent(map, kaggle, rhapsodeCollection);
    }

    private void dumpContent(Map<String, PubMetadata> map, Path kaggle, Path rhapsodeCollection) throws Exception {
        for (File dir : kaggle.toFile().listFiles()) {
            if (! dir.isDirectory()) {
                continue;
            }
            for (File subdir : dir.listFiles()) {
                if (! subdir.isDirectory()) {
                    continue;
                }
                for (File file : subdir.listFiles()) {
                    dumpFile(file, map, rhapsodeCollection);
                }
            }
        }
    }

    private void dumpFile(File file, Map<String, PubMetadata> map, Path rhapsodeCollection) throws IOException, TikaException {
        String content = getText(file);
        String sha = file.getName().replaceAll(".json", "");
        PubMetadata pubMetadata = map.get(sha);
        pubMetadata.content = content;
        writeOut(pubMetadata, rhapsodeCollection);

    }

    private String getText(File file) throws IOException {
        JsonElement root = null;
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader);
        }
        List<String> lines = new ArrayList<>();
        for (String k : new String[]{
                //"abstract", this field is typically pretty bad; better to grab abstract from metadata csv
                "body_text"}) {
            JsonArray arr = root.getAsJsonObject().get(k).getAsJsonArray();
            for (JsonElement item : arr) {
                String txt = item.getAsJsonObject().get("text").getAsString();
                lines.add(txt + "\n");
            }
            lines.add("\n");
        }
        return StringUtils.joinWith("", lines);
    }

    private void dumpMissingContent(Map<String, PubMetadata> map,
                                    Path rhapsodeCollection) throws Exception {
        for (String key : map.keySet()) {
            if (key.startsWith(missingShaPrefix)) {
                writeOut(map.get(key), rhapsodeCollection);
            }
        }
    }

    private void writeOut(PubMetadata pubMetadata, Path rhapsodeCollection) throws IOException, TikaException {
        Path txtFile = rhapsodeCollection.resolve("input/"
                +pubMetadata.collection+"/"+pubMetadata.sha+".txt");
        if (! Files.isDirectory(txtFile.getParent())) {
            Files.createDirectories(txtFile.getParent());
        }
        String txt = StringUtils.joinWith("\n\n",pubMetadata.title,
                pubMetadata.authors, pubMetadata.journal, pubMetadata.abstrct, pubMetadata.content);
        Files.writeString(txtFile, txt, StandardCharsets.UTF_8);


        Metadata metadata = new Metadata();
        Path extractedText = rhapsodeCollection.resolve("extracted_text/"+
                pubMetadata.collection+"/"+pubMetadata.sha+".txt.json");
        if (! Files.isDirectory(extractedText.getParent())) {
            Files.createDirectories(extractedText.getParent());
        }
        metadata.set(TikaCoreProperties.CREATOR, pubMetadata.authors.split("; "));
        metadata.set(TikaCoreProperties.TITLE, pubMetadata.title);
        metadata.set(TikaCoreProperties.PUBLISHER, pubMetadata.journal);
        metadata.set("abstract", pubMetadata.abstrct);
        metadata.set("collection", pubMetadata.collection);
        metadata.set("doi", pubMetadata.doi);
        metadata.set("pmcid", pubMetadata.pmcid);
        metadata.set("license", pubMetadata.license);
        metadata.set("source_x", pubMetadata.sourcex);
        metadata.set("sha", pubMetadata.sha);
        metadata.set(AbstractRecursiveParserWrapperHandler.TIKA_CONTENT,
                pubMetadata.abstrct+"\n\n"+
                pubMetadata.content);
        metadata.set(TikaCoreProperties.CREATED, pubMetadata.publish_time);
        metadata.set(Metadata.CONTENT_TYPE, "text/plain");
        metadata.set("resourceName", txtFile.getFileName().toString());
        metadata.set("tika_batch_fs:relative_path", pubMetadata.collection+"/"+pubMetadata.sha+".txt");

        try (Writer writer = Files.newBufferedWriter(extractedText, StandardCharsets.UTF_8)) {
            JsonMetadataList.toJson(Collections.singletonList(metadata), writer);
        }
    }

    private Map<String, PubMetadata> loadMetadata(Path kaggle) throws Exception {
        Path metadata = kaggle.resolve("metadata.csv");
        Map<String, PubMetadata> map = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(metadata, StandardCharsets.UTF_8)) {
            for (CSVRecord r : CSVFormat.EXCEL.withFirstRecordAsHeader().parse(reader)) {
                String[] shas = r.get("sha").split("; ");
                for (String sha : shas) {
                    PubMetadata m = new PubMetadata();
                    if (StringUtils.isBlank(sha)) {
                        sha = missingShaPrefix+missingSha++;
                    }
                    System.out.println(sha);
                    m.sha = sha;
                    m.sourcex = r.get("source_x");
                    m.title = r.get("title");
                    m.doi = r.get("doi");
                    m.pmcid = r.get("pmcid");
                    m.pubmed_id = r.get("pubmed_id");
                    m.license = r.get("license");
                    m.authors = r.get("authors");
                    m.journal = r.get("journal");
                    m.publish_time = r.get("publish_time");
                    m.collection = r.get("full_text_file");
                    if (StringUtils.isAllBlank(m.collection)) {
                        m.collection = "no_content";
                    }
                    m.abstrct = r.get("abstract");
                    map.put(sha, m);
                }
            }
        }
        return map;
    }


    private class PubMetadata {
        //sha	source_x	title	doi	pmcid	pubmed_id	license	abstract	publish_time	authors	journal	Microsoft Academic Paper ID	WHO #Covidence	has_full_text	full_text_file
        String sha;
        String sourcex;
        String title;
        String doi;
        String pmcid;
        String pubmed_id;
        String license;
        String abstrct;
        String publish_time;
        String authors;
        String journal;
        String collection;
        String content;

    }

}
