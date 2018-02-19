package space.exploration.mars.rover.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RemsDataService {
    public static final String URL    = "http://pds-atmospheres.nmsu.edu/PDS/data/mslrem_1001/DATA/";
    private static      Logger logger = LoggerFactory.getLogger(RemsDataService.class);

    private static String getSolRangeLink(int sol) {
        List<String> pageLinks = new ArrayList<>();
        try {
            Document responseDoc = Jsoup.connect(URL).get();
            for (Element element : responseDoc.select("a[href]")) {
                pageLinks.add(element.text());
            }
        } catch (Exception e) {
            logger.error(" Could not find data files for sol = " + 12 + " url = " + URL, e);
        }

        for (String s : pageLinks) {
            String[] splits = s.split("_");
            if (splits[0].equals("SOL")) {
                int beginSol = Integer.parseInt(splits[1].replaceAll("/", ""));
                int endSol   = Integer.parseInt(splits[2].replaceAll("/", ""));
                if (beginSol <= sol && endSol >= sol) {
                    return s;
                }
            }
        }

        return null;
    }

    private static String getSolLink(int sol, String url) {
        try {
            Document responseDoc = Jsoup.connect(url).get();
            for (Element element : responseDoc.select("a[href]")) {
                if (element.text().contains("SOL")) {
                    String solText = element.text();
                    int    solNum  = Integer.parseInt(solText.replaceAll("SOL", "").replaceAll("/", ""));
                    if (sol == solNum) {
                        return solText;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Did not find matching sol link.", e);
        }
        return null;
    }

    private static String getFileLink(String pathLink) {
        try {
            Document responseDoc = Jsoup.connect(pathLink).get();
            for (Element element : responseDoc.select("a[href]")) {
                if (element.text().contains("RME") && element.text().contains("RNV") && element.text().contains
                        ("TAB")) {
                    return element.text();
                }
            }

        } catch (Exception e) {
            logger.error("Can not find relevant link here for target = " + pathLink, e);
        }

        return null;
    }

    public static String getFileURL(int sol) {
        String solRange = getSolRangeLink(sol);
        String solLink  = getSolLink(sol, URL + solRange);
        String finalURL = getFileLink(URL + solRange + solLink);
        return (URL + solRange + solLink + finalURL);
    }
}
