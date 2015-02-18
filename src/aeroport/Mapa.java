package aeroport;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;

public class Mapa extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	private int cityCmWidth;
    private int cityCmHeight;
    private int mapWidth;
    private int mapHeigth;
    private float factorX, factorY;
    private float zoomLevel;
    private int offsetX, offsetY;
    private int cmVCarrerWidth, cmHCarrerWidth, cmCarrerMark;
    private BufferedImage imgMap, imgFondo, imgAeroport, imgVent;
    private Aeroport aeroport;
    private ArrayList<Carrer> carrers;
    private ArrayList<CrossRoad> crossroads;
    private ArrayList<Finger> fingers;
    private HCarrer goFingers;
    private int vent;

    public Mapa(int cityCmWidth, int cityCmHeight, int mapPixWidth, int mapPixHeight, Aeroport aeroport) {
    	
//    	vent = ((int) (Math.random()*2));
    	vent = 0;
    	this.aeroport = aeroport;
        this.cityCmWidth = cityCmWidth;
        this.cityCmHeight = cityCmHeight;
        mapWidth = mapPixWidth;
        mapHeigth = mapPixHeight;
      
        offsetX = 0;
        offsetY = 0;
        zoomLevel = 2;
        setFactorXY();

        cmVCarrerWidth = 800;
        cmHCarrerWidth = cmVCarrerWidth+200;
        cmCarrerMark = 300;
        
        goFingers=new HCarrer("goFingers", 1, cmHCarrerWidth, cmCarrerMark, 12000, 11000, 9000);
        
        carrers = new ArrayList<Carrer>();
        crossroads = new ArrayList<CrossRoad>();
       
        loadCarrers();
        
 /*
         *     *	*	*	*
 */
        Dimension d = new Dimension(1336, 520);
        setPreferredSize(d);
        
        fingers= new ArrayList<Finger>();
        loadFingers();
        
        calculateCrossRoads();
        
        try {
            imgFondo = ImageIO.read(new File("Imagenes/fondo.png"));
            imgAeroport = ImageIO.read(new File("Imagenes/terminal.png"));
            imgVent = ImageIO.read(new File("Imagenes/viento.png"));
        } catch (IOException e) {
            System.out.println("Img Error: not found");
        }
    }

    public ArrayList<Carrer> getCarrers() {
        return this.carrers;
    }
    
    public ArrayList<Finger> getFingers() {
        return this.fingers;
    }

    public void setWidth(int mapWidth) {
        this.mapWidth = mapWidth;
        this.setFactorXY();
    }

    public void setHeig(int mapHeigth) {
        this.mapHeigth = mapHeigth;
        this.setFactorXY();
    }


    public void setFactorXY() {
        this.mapWidth = this.getWidth();
        this.mapHeigth = this.getHeight();

        this.factorX = ((float) this.cityCmWidth / (float) this.mapWidth / this.zoomLevel);
        this.factorY = ((float) this.cityCmHeight / (float) this.mapHeigth / this.zoomLevel);
        this.paintImgMap();
    }

    private boolean addCrossRoad(CrossRoad newCr) {
        Iterator<CrossRoad> itr = this.crossroads.iterator();
        while (itr.hasNext()) {
            if (itr.next().equals(newCr)) {
                return false;  // ====== Crossroad duplicated ================>>
            }
        }

        this.crossroads.add(newCr);
        return true;
    }

    private void calculateCrossRoads() {
        Iterator<Carrer> itrCarrers1;
        Iterator<Carrer> itrCarrers2;
        Carrer auxCarrer1, auxCarrer2;
       
        itrCarrers1 = this.carrers.iterator();
        while (itrCarrers1.hasNext()) {
            auxCarrer1 = itrCarrers1.next();

            itrCarrers2 = this.carrers.iterator();
            if (auxCarrer1 instanceof HCarrer){
                while (itrCarrers2.hasNext()) {
                    auxCarrer2 = itrCarrers2.next();
                    if (auxCarrer2 instanceof VCarrer){
                    	if (auxCarrer1.carrerIntersection(auxCarrer2)) {
                    		this.addCrossRoad(new CrossRoad(auxCarrer1, auxCarrer2));
                    	}
                    }
                }
            }
        }
        
        for (int i = 0; i < this.fingers.size(); i++) {
        	 this.addCrossRoad(new CrossRoad(goFingers, fingers.get(i)));
		}
    }

    public void moveRight() {
        offsetX += 100;
        setFactorXY();
    }

    public void moveLeft() {
        offsetX -= 100;
        setFactorXY();
    }

    public void moveDown() {
        offsetY += 100;
        setFactorXY();
    }

    public void moveUp() {
        offsetY -= 100;
        setFactorXY();
    }

    public synchronized void paint() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            return; 
        }
        
        if ((this.mapWidth < 0) || (this.mapHeigth < 0)) {
            System.out.println("Map size error: (" + mapWidth + "," + mapHeigth + ")");
            return; 
        }        
        
        Graphics gg = bs.getDrawGraphics();

        gg.drawImage(imgMap, 0, 0, mapWidth, mapHeigth, null);
        paintAvions(gg);

        bs.show();

        gg.dispose();
    }

	public void paintBackgroud(Graphics g) {
    	 int iniX, iniY, finX, finY;
         
         iniX = (int)((0 / factorX) + offsetX);
         iniY = (int)((0 / factorY) + offsetY);
         finX = (int)((60000 / factorX));
         finY = (int)((40000 / factorY));  
         

        g.drawImage(imgFondo, iniX, iniY, finX, finY, null);
    }

    public void paintCrossRoads(Graphics g) {
        Iterator<CrossRoad> itr = crossroads.iterator();

        while (itr.hasNext()) {
            itr.next().paint(g, factorX, factorY, offsetX, offsetY);
        }
    }
    
    public void paintCarrers(Graphics g) {
        Iterator<Carrer> itr = carrers.iterator();

        while (itr.hasNext()) {
            itr.next().paint(g, factorX, factorY, offsetX, offsetY);
        }
    }
    
    public void run() {
        this.createBufferStrategy(2);

        while (!Aeroport.isEnd()) {
        	if(!Aeroport.isPaused()){
	            this.paint();
                try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
        	}
        }
    }

    public void zoomIn() {
        zoomIn(0.01f);
    }

    public void zoomIn(float inc) {
        zoomLevel += inc;
        setFactorXY();

    }

    public void zoomOut() {
        zoomOut(0.01f);
        setFactorXY();
    }

    public void zoomOut(float inc) {
        zoomLevel -= inc;
        setFactorXY();
    }

    public void zoomReset() {
        zoomLevel = 1;
        setFactorXY();
    }
    
    public synchronized void paintImgMap() {
        if ((mapWidth <= 0) || (mapHeigth <= 0)) {
            System.out.println("Map size error: (" + mapWidth + "," + mapHeigth + ")");
            return; 
        }

        imgMap = new BufferedImage(mapWidth, mapHeigth, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imgMap.createGraphics();
        
        
        paintBackgroud(g);          
        paintTerminal(g, factorX, factorY, offsetX, offsetY);
        paintFingers(g);
        paintCarrers(g);
        paintCrossRoads(g);
        paintVent(g, factorX, factorY,offsetX, offsetY);
        g.dispose();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private void loadCarrers() {
    	//String idWay, int maxAvions, int cmWayWidth, int cmWayMark, int cmLong, int cmPosIniX, int cmPosIniY
    	carrers.add(new HCarrer("pista", 1, cmHCarrerWidth+600, cmCarrerMark, 22000, 3000, 500));
    	carrers.add(new HCarrer("h2", 0, cmHCarrerWidth, cmCarrerMark, 18000, 5000, 4000));
    	carrers.add(new VCarrer("iniciPista", 0, cmVCarrerWidth, cmCarrerMark, 6100, 5000, 500));
        carrers.add(new VCarrer("fiPista", 0, cmVCarrerWidth, cmCarrerMark, 9000, 22210, 500));
        carrers.add(new HCarrer("h1", 0, cmHCarrerWidth, cmCarrerMark, 6100, 5000, 6500));
        carrers.add(goFingers);
        carrers.add(new VCarrer("v1", 0, cmVCarrerWidth, cmCarrerMark, 3000, 11000, 6500));
    }
    
    private void loadFingers(){
    	// int id, int pos x, int pos y, estat
    	int posx=12000;
    	for (int i = 0; i < 7 ; i++) {
			this.fingers.add(new Finger("finger"+i+"", 1000, this.cmCarrerMark, 2500, posx, 9000));
			posx+=1500;
		}
    }
    
    public void paintFingers(Graphics g){
    	for (int i = 0; i < this.fingers.size(); i++) {
    		fingers.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
    }
    
    public void paintTerminal(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int iniX, iniY, finX, finY;
        
        iniX = (int)((8000 / factorX) + offsetX);
        iniY = (int)((9000 / factorY) + offsetY);
        finX = (int)((15000 / factorX));
        finY = (int)((7000 / factorY));  
       
    	g.drawImage(imgAeroport, iniX, iniY, finX, finY, this);
    }
    
    public void paintVent(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int iniX, iniY, finX, finY;
        
        iniX = (int)((16000 / factorX) + offsetX);
        iniY = (int)((4000 / factorY) + offsetY);
        finX = (int)((4000 / factorX));
        finY = (int)((4000 / factorY));  
       
        if(vent == 0) g.drawImage(imgVent, iniX, iniY, -finX, finY, this);
		else if(vent ==1) g.drawImage(imgVent, iniX, iniY, finX, finY, this);
    	
    }
    
    private void paintAvions(Graphics g){
    	aeroport.paintAvions(g, factorX, factorY, offsetX, offsetY);
    }

	public int getVent() {
		return vent;
	}
}