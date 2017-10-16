package space.exploration.mars.rover.dataUplink;

public class PhotoQueryService extends QueryService {
    private String camId  = "";

    public void setCamId(String camId) {
        this.camId = camId;
    }

    @Override
    public String getQueryString() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?earth_date=");
        queryBuilder.append(earthDate);
        queryBuilder.append("&camera=");
        queryBuilder.append(camId);
        queryBuilder.append("&api_key=");
        queryBuilder.append(authenticationKey);

        return queryBuilder.toString();
    }
}