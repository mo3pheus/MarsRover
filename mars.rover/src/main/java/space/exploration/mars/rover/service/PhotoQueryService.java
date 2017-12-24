package space.exploration.mars.rover.service;

public class PhotoQueryService extends QueryService {
    private String camId = "";
    private int    sol   = 0;

    public void setCamId(String camId) {
        this.camId = camId;
    }

    public void setSol(int sol) {
        this.sol = sol;
    }

    @Override
    public String getQueryString() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos?sol=");
        queryBuilder.append(Integer.toString(sol));
        queryBuilder.append("&camera=");
        queryBuilder.append(camId);
        queryBuilder.append("&api_key=");
        queryBuilder.append(authenticationKey);

        return queryBuilder.toString();
    }
}