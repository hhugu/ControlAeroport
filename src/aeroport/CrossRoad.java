package aeroport;


import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author Jumi
 */
public class CrossRoad {
    private String idCrossRoad;    
    private int cmIniX, cmIniY, cmFinX, cmFinY;
    private int cmWidthX, cmWidthY;
    
    private Carrer hCarrer;
    private Carrer vCarrer;
    
    public CrossRoad(Carrer Carrer1, Carrer Carrer2) {        
        this.setCarrers(Carrer1, Carrer2);
        this.setDimension();
        this.setPosition();

        // Actualize crossferences
        this.hCarrer.addCrossRoad(this);
        this.vCarrer.addCrossRoad(this);
    }
    
    public String getId() {
        return this.idCrossRoad;
    }
    
    public int getIniX() {        
        return this.cmIniX;
    }
    
    public int getFinX() {        
        return this.cmFinX;
    }
    
    public int getIniY() {        
        return this.cmIniY;
    }
    
    public int getFinY() {        
        return this.cmFinY;
    }
    
    public Carrer getVCarrer() {
        return this.vCarrer;
    }
    
    public Carrer getHCarrer() {
        return this.hCarrer;
    }
    
    public Carrer getCarrer(Carrer c){
    	if (c.equals(vCarrer)) return hCarrer;
		else return vCarrer;
    }

    private void setCarrers(Carrer Carrer1, Carrer Carrer2) {
        if (Carrer1 instanceof VCarrer) {
            this.vCarrer = Carrer1;
        }

        if (Carrer1 instanceof HCarrer) {
            this.hCarrer = Carrer1;
        }

        if (Carrer2 instanceof VCarrer) {
            this.vCarrer = Carrer2;
        }

        if (Carrer2 instanceof HCarrer) {
            this.hCarrer = Carrer2;
        }
        
        if(Carrer1 instanceof Finger){
        	this.vCarrer = Carrer1;
        }
        
        if(Carrer2 instanceof Finger){
        	this.vCarrer = Carrer2;
        }
        
        this.idCrossRoad = this.vCarrer.getId() + "·" + this.hCarrer.getId();
    }

    private void setDimension() {
        this.cmWidthX = this.vCarrer.cmWidth;
        this.cmWidthY = this.hCarrer.cmWidth;
    }
    
    private void setPosition() {
        this.cmIniX = this.vCarrer.cmIniX;
        this.cmFinX = this.cmIniX + this.cmWidthX;
        this.cmIniY = this.hCarrer.cmIniY;
        this.cmFinY = this.cmIniY + this.cmWidthY;
    }
    
    public String toString() {
        return "Crossroad :"+this.getId();
    }
    
    public void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int iniX, iniY, finX, finY;
        
        iniX = (int)((this.cmIniX / factorX) + offsetX);
        iniY = (int)((this.cmIniY / factorY) + offsetY);
        finX = (int)((this.cmWidthX / factorX));
        finY = (int)((this.cmWidthY / factorY));                
                
        // Paint crossroad
        g.setColor(Color.DARK_GRAY);
        g.fillRect(iniX, iniY, finX, finY);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(iniX, iniY, finX, finY);

    }
}