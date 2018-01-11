package space.exploration.mars.rover.service;

public class DANService extends QueryService{
    private int sol;

    public DANService(int sol){
        this.sol = sol;
    }

    @Override
    public String getQueryString() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("http://pds-geosciences.wustl.edu/msl/msl-m-dan-3_4-rdr-v1/msldan_1xxx/data/sol0240/");
        return queryBuilder.toString();
    }
}
