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
    private int cmVCarrerWidth, cmHCarrerWidth;
    private BufferedImage imgMap, imgFondo, imgAeroport, imgVent;
    private Aeroport aeroport;
    private ArrayList<Carrer> carrers;
    private ArrayList<CrossRoad> crossroads;
    private ArrayList<Finger> fingers;
    private HCarrer goFingers;
    private int vent;

    public Mapa(int cityCmWidth, int cityCmHeight, int mapPixWidth, int mapPixHeight, Aeroport aeroport) {
    	
//    	vent = ((int) (Math.random()*2));
    	vent = 1;
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
        
        goFingers=new HCarrer("goFingers", false, cmHCarrerWidth, 12000, 11000, 9000);
        
        carrers = new ArrayList<Carrer>();
        crossroads = new ArrayList<CrossRoad>();
       
        loadCarrers();
        
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

    /*
     * PAINT
     */
    
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
    /**
     * Pinta l'imatge de fondo en funcio del tros de mapa que es visualitza i del zoom que te.
     * @param g - Graphics
     */
	public void paintBackgroud(Graphics g) {
    	 int iniX, iniY, finX, finY;
         
         iniX = (int)((0 / factorX) + offsetX);
         iniY = (int)((0 / factorY) + offsetY);
         finX = (int)((60000 / factorX));
         finY = (int)((40000 / factorY));  
         

        g.drawImage(imgFondo, iniX, iniY, finX, finY, null);
    }

	/**
	 * Helper que crida a pintar tots els cruces.
	 * @param g - Graphics
	 */
    public void paintCrossRoads(Graphics g) {
        Iterator<CrossRoad> itr = crossroads.iterator();

        while (itr.hasNext()) {
            itr.next().paint(g, factorX, factorY, offsetX, offsetY);
        }
    }
    
    /**
	 * Helper que crida a pintar tots els carrers.
	 * @param g - Graphics
	 */
    public void paintCarrers(Graphics g) {
        Iterator<Carrer> itr = carrers.iterator();

        while (itr.hasNext()) {
            itr.next().paint(g, factorX, factorY, offsetX, offsetY);
        }
    }
    
    /**
	 * Helper que crida a pintar tots els fingers.
	 * @param g - Graphics
	 */
    public void paintFingers(Graphics g){
    	for (int i = 0; i < this.fingers.size(); i++) {
    		fingers.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
    }
    
    /**
     * Metode per pintar la terminal, una foto.
     * @param g - Graphics
     * @param factorX - float
     * @param factorY - float
     * @param offsetX - int
     * @param offsetY - int
     */
    public void paintTerminal(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int iniX, iniY, finX, finY;
        
        iniX = (int)((8000 / factorX) + offsetX);
        iniY = (int)((9000 / factorY) + offsetY);
        finX = (int)((15000 / factorX));
        finY = (int)((7000 / factorY));  
       
    	g.drawImage(imgAeroport, iniX, iniY, finX, finY, this);
    }
    
    /**
     * Metode per pintar la maniga que indica d'on ve el vent, una foto.
     * @param g - Graphics
     * @param factorX - float
     * @param factorY - float
     * @param offsetX - int
     * @param offsetY - int
     */
    public void paintVent(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
        int iniX, iniY, finX, finY;
        
        iniX = (int)((16000 / factorX) + offsetX);
        iniY = (int)((4000 / factorY) + offsetY);
        finX = (int)((4000 / factorX));
        finY = (int)((4000 / factorY));  
       
        if(vent == 0) g.drawImage(imgVent, iniX, iniY, -finX, finY, this);
		else if(vent ==1) g.drawImage(imgVent, iniX, iniY, finX, finY, this);
    	
    }
    
    /**
     * Helper que crida al controlador a pinta els avions. 
     * @param g - Graphics
     */
    private void paintAvions(Graphics g){
    	aeroport.paintAvions(g, factorX, factorY, offsetX, offsetY);
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
    
    

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /*
     * CREACIO CARRERS, FINGERS Y CRUCES
     */

    /**
     * Metode que s'encarrega de calcular on es creuen els carrers horizontals amb els verticals o amb els fingers
     * y crear un cruce en el punt on es creuen.
     */
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
    
    /**
     * Helper que s'encarrega de comprovar si el cruce existeix.
     * @param newCr - CrossRoad
     * @return boolean - false si existeix, true si no
     */
    private boolean addCrossRoad(CrossRoad newCr) {
        Iterator<CrossRoad> itr = this.crossroads.iterator();
        while (itr.hasNext()) {
            if (itr.next().equals(newCr)) {
                return false;
            }
        }
        this.crossroads.add(newCr);
        return true;
    }
    
    /**
     * Metode on es creen i s'afegeixen a l'arrayList de carrers els carrers que tendra el mapa.
     */
    private void loadCarrers() {
    	//String idWay, int maxAvions, int cmWayWidth, int cmWayMark, int cmLong, int cmPosIniX, int cmPosIniY
    	carrers.add(new HCarrer("pista", true, cmHCarrerWidth+600, 22000, 3000, 500));
    	carrers.add(new HCarrer("h2", false, cmHCarrerWidth, 18000, 5000, 4000));
    	carrers.add(new VCarrer("iniciPista", false, cmVCarrerWidth, 6100, 5000, 500));
        carrers.add(new VCarrer("fiPista", false, cmVCarrerWidth, 9000, 22210, 500));
        carrers.add(new HCarrer("h1", false, cmHCarrerWidth, 6100, 5000, 6500));
        carrers.add(goFingers);
        carrers.add(new VCarrer("v1", false, cmVCarrerWidth, 3000, 11000, 6500));
    }
    
    /**
     * Metode on es creen i s'afegeixen a l'arrayList de fingers els fingers que tendra el mapa.
     */
    private void loadFingers(){
    	// String id, int pos x, int pos y, estat
    	int posx=12000;
    	for (int i = 0; i < 7 ; i++) {
			this.fingers.add(new Finger("finger"+i+"", 1000, 2500, posx, 9000));
			posx+=1500;
		}
    }

    /*
     * GETTERSY SETTERS
     */
    
	public int getVent() {
		return vent;
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
}