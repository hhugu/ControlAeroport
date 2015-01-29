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

import aeroport.Avio.Direction;

public class Mapa extends Canvas implements Runnable {

	private static final long serialVersionUID = 4571448590220527937L;
	private int cityCmWidth;
    private int cityCmHeight;
    private int mapWidth;
    private int mapHeigth;
    private float factorX;
    private float factorY;
    private float zoomLevel;
    private int offsetX;
    private int offsetY;
    private int cmVCarrerWidth, cmHCarrerWidth;
    private int cmCarrerMark;
    private BufferedImage imgMap, fondo, aeropuerto;
    private Controlador controlador;
    private ArrayList<Carrer> carrers;
    private ArrayList<CrossRoad> crossroads;
    private ArrayList<Finger> fingers;
    private HCarrer goFingers;

    public Mapa(int cityCmWidth, int cityCmHeight, int mapPixWidth, int mapPixHeight) {
    	
    	
    	
        this.cityCmWidth = cityCmWidth;
        this.cityCmHeight = cityCmHeight;
        this.mapWidth = mapPixWidth;
        this.mapHeigth = mapPixHeight;
      
        this.offsetX = 0;
        this.offsetY = 0;
        this.zoomLevel = 2;
        this.setFactorXY();

        this.cmVCarrerWidth = 800;
        this.cmHCarrerWidth = cmVCarrerWidth+200;
        this.cmCarrerMark = 300; // Longitud marcas viales en cm
        
        goFingers=new HCarrer("goFingers",this.cmHCarrerWidth, this.cmCarrerMark, 12000, 11000, 9000, Direction.FORWARD);
        
        this.carrers = new ArrayList<Carrer>();
        this.crossroads = new ArrayList<CrossRoad>();
       
        this.loadCarrers();
        
 /*
         *     *	*	*	*
 */
        Dimension d = new Dimension(1336, 520);
        this.setPreferredSize(d);
        
        this.fingers= new ArrayList<Finger>();
        this.loadFingers();
        
        this.calculateCrossRoads();
        
        try {
            this.fondo = ImageIO.read(new File("Imagenes/fondo.png"));
            this.aeropuerto = ImageIO.read(new File("Imagenes/terminal.png"));
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
        BufferStrategy bs;
        bs = getBufferStrategy();
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
         finX = (int)((30000 / factorX));
         finY = (int)((20000 / factorY));  
         

        g.drawImage(fondo, iniX, iniY, finX, finY, null);
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
	
	            do {
	                try {
	                    Thread.sleep(7); // nano -> ms
	                } catch (InterruptedException ex) {
	                }
	            } while (Aeroport.isPaused());
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
        g.dispose();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private void loadCarrers() {
    	//String idWay, int cmWayWidth, int cmWayMark, int cmLong, int cmPosIniX, int cmPosIniY , Direction
    	carrers.add(new HCarrer("pista", cmHCarrerWidth+600, cmCarrerMark, 22000, 3000, 500, null));
    	carrers.add(new HCarrer("h2", cmHCarrerWidth, cmCarrerMark, 18000, 5000, 2500, null));
    	carrers.add(new VCarrer("iniciPista", cmVCarrerWidth, cmCarrerMark, 6000, 5000, 500, Direction.FORWARD));
        carrers.add(new VCarrer("fiPista", cmVCarrerWidth, cmCarrerMark, 9000, 22210, 500, Direction.BACKWARD));
        carrers.add(new HCarrer("h1", cmHCarrerWidth, cmCarrerMark, 6090, 5000, 6500, Direction.FORWARD));
        carrers.add(goFingers);
        carrers.add(new VCarrer("v1", cmVCarrerWidth, cmCarrerMark, 3000, 11000, 6500, Direction.FORWARD));
    }
    
    private void loadFingers(){
    	// int id, int pos x, int pos y, estat
    	int posx=12000;
    	for (int i = 0; i < 7 ; i++) {
			this.fingers.add(new Finger("finger"+i+"", 1000, this.cmCarrerMark, 2500, posx, 9000, Direction.FORWARD));
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
       
    	g.drawImage(aeropuerto, iniX, iniY, finX, finY, this);
    }
    
    private void paintAvions(Graphics g){
    	controlador.paintAvions(g, factorX, factorY, offsetX, offsetY);
    }

	public void setControlador(Controlador traffic) {
		controlador=traffic;
	}
}