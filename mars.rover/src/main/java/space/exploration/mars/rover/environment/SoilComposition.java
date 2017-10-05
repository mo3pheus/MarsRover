package space.exploration.mars.rover.environment;

/**
 * Created by sanketkorgaonkar on 5/2/17.
 */
public class SoilComposition {

    private double Na2O;
    private double FeO;
    private double MgO;
    private double Al2O3;
    private double H2O;

    public SoilComposition(double Na2O, double FeO, double MgO, double Al2O3) {
        this.Al2O3 = Al2O3;
        this.Na2O = Na2O;
        this.FeO = FeO;
        this.MgO = MgO;
        this.H2O = 0.0d;
    }

    public SoilComposition(double h2o) {
        this.Al2O3 = 0.0d;
        this.Na2O = 0.0d;
        this.FeO = 0.0d;
        this.MgO = 0.0d;
        this.H2O = h2o;
    }

    public double getNa2O() {

        return Na2O;
    }

    public void setNa2O(double na2O) {
        Na2O = na2O;
    }

    public double getFeO() {
        return FeO;
    }

    public void setFeO(double feO) {
        FeO = feO;
    }

    public double getMgO() {
        return MgO;
    }

    public void setMgO(double mgO) {
        MgO = mgO;
    }

    public double getAl2O3() {
        return Al2O3;
    }

    public void setAl2O3(double al2O3) {
        Al2O3 = al2O3;
    }

    public double getH2O() {
        return H2O;
    }

    public void setH2O(double h2O) {
        H2O = h2O;
    }

    public String toString() {
        return " Al2O3 = " + Al2O3 + " Na2O = " + Na2O + " FeO = " + FeO + " MgO = " + MgO + " H2O = " + H2O;
    }
}
