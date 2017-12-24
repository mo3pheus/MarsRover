package space.exploration.mars.rover.service;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class QueryService implements isSpaceQuery {

    public static final String            DATE_FORMAT       = "YYYY-MM-dd";
    protected           HttpURLConnection dataLink          = null;
    protected           String            authenticationKey = null;
    protected           int               solNumber         = -1;
    protected           DateTime          earthStartDate    = null;
    protected           DateTime          earthEndDate      = null;
    protected           String            erthStartDate     = null;
    protected           String            earthDateEnd      = null;
    protected           DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);
    protected           Logger            logger            = LoggerFactory.getLogger(QueryService.class);

    @Override
    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    @Override
    public void setEarthStartDate(long startMs) {
        this.earthStartDate = new DateTime(startMs);
        this.erthStartDate = dateTimeFormatter.print(startMs);
        logger.debug("StartMS supplied = " + startMs + " jodaInternalEarthDate = " + earthStartDate + " " +
                             "curiosityEarthDate = " + erthStartDate);
    }

    public void setEarthStartDate(String earthStartDate) {
        this.erthStartDate = earthStartDate;
    }

    public void setEarthEndDate(String earthDateEnd) {
        this.earthDateEnd = earthDateEnd;
    }

    @Override
    public void setEarthEndDate(long endMs) {
        this.earthEndDate = new DateTime(endMs);
        this.earthDateEnd = dateTimeFormatter.print(endMs);
        logger.debug("EndDateMS supplied = " + endMs + " internal date = " + earthDateEnd);
    }

    @Override
    public Object getQueryResponseType() {
        return dataLink.getContentType();
    }

    @Override
    public String getQueryString() {
        return "";
    }

    @Override
    public void executeQuery() {
        logger.debug(getTargetUrl().toString());
        try {
            dataLink = (HttpURLConnection) getTargetUrl().openConnection();
            dataLink.setRequestMethod("GET");
        } catch (IOException e) {
            logger.error("Could not execute query for cameraAPI", e);
        }
    }

    @Override
    public Object getResponse() {
        try {
            return dataLink.getContent();
        } catch (IOException e) {
            logger.error("IOException when getting queryResponse ", e);
            return null;
        }
    }

    @Override
    public String getResponseAsString() {
        BufferedReader reader          = null;
        StringBuilder  responseBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(dataLink.getInputStream()));
            String dataLine = null;
            while ((dataLine = reader.readLine()) != null) {
                responseBuilder.append(dataLine);
            }
        } catch (IOException io) {
            logger.error("IOException", io);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }
        return responseBuilder.toString();
    }

    private URL getTargetUrl() {
        URL    url         = null;
        String queryString = "";
        try {
            queryString = getQueryString();
            if (erthStartDate != null && earthDateEnd != null) {
                queryString += "terrestrial_date_end=" + earthDateEnd
                        + "&terrestrial_date_start=" + erthStartDate;
            } else if (solNumber != -1) {
                queryString += "sol=" + Integer.toString(solNumber);
            }

            logger.debug(queryString);
            url = new URL(queryString);
        } catch (MalformedURLException malFormedURL) {
            logger.error("URL was malformed.", malFormedURL);
        }
        return url;
    }

    public void setSolNumber(int solNumber) {
        this.solNumber = solNumber;
    }
}