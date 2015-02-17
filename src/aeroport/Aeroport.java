package aeroport;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;

import aeroport.Avio.Direction;
import aeroport.Finger.Estat;


public class Aeroport extends JFrame implements Runnable, MouseWheelListener, ActionListener, ComponentListener {
	
    @SuppressWarnings("unused")
	public static void main(String[] args) {Aeroport ciutat = new Aeroport();}
    
   private static final long serialVersionUID = 1864146382345156552L;
	public static final int CIUTAT_CM_WIDTH = 60000; // Amplada ciutat
    public static final int CIUTAT_CM_HEIGHT = 34500; // Al�ada ciutat
    public static final int FRAME_PIX_WIDTH = 1080; // Amplada window
    public static final int FRAME_PIX_HEIGHT = 620;  // Al�ada window 
    public static final int MAPA_PIX_WIDTH = 1080; // Amplada window
    public static final int MAPA_PIX_HEIGH = 620;  // Al�ada window 
   private static final int MAX_AVIONS = 10;
	public static final int SLEEP_TIME = 20;
	public static final int VELOCITAT_SEGURITAT = 50, VELOCITAT_DE_VOL = 460; 


    private static volatile boolean pauseCity;
    private static volatile boolean endCity;
        
    private ArrayList <Avio> avions = new ArrayList<Avio>();
	private ArrayList <Carrer> carrers;
	private ArrayList <Finger> fingers;
	private ArrayList <String> rutaAlFingerOest, rutaDespegueOest, rutaAlFingerEst, rutaDespegueEst;
	
    private Mapa mapa;
    
    public Aeroport() {
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
    	
        mapa = new Mapa(Aeroport.CIUTAT_CM_WIDTH, Aeroport.CIUTAT_CM_HEIGHT, Aeroport.MAPA_PIX_WIDTH, Aeroport.MAPA_PIX_HEIGH, this);
        
        carrers = mapa.getCarrers();
        fingers = mapa.getFingers();
        
        createFrame();
        crearRutes();
		
        new Thread(mapa).start();
        new Thread(this).start();
    }
    
    /*
     * CARREGAR PART GRAFICA
     */
    
    private void createFrame() {
        Container panel;
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());

        panel = getContentPane();
        addMapToPane(panel);
        addButtonsToPane(panel);
       
        panel.addMouseWheelListener(this);
        
        pack();
        setVisible(true);

        addComponentListener(this);
    }
    
    private void addButtonsToPane(Container pane) {
    	JButton bUp = new JButton("Up");
    	JButton bDown = new JButton("Down");
    	JButton bLeft = new JButton("<");
    	JButton bRight = new JButton(">");
    	JButton bZoomPlus = new JButton("Z+");
    	JButton bZoomMinus = new JButton("Z-");
    	JButton pause = new JButton("Pause");
        JButton start = new JButton("Start");
        JButton end = new JButton("End");

        bUp.addActionListener(this);
        bDown.addActionListener(this);
        bLeft.addActionListener(this);
        bRight.addActionListener(this);
        bZoomPlus.addActionListener(this);
        bZoomMinus.addActionListener(this);
        pause.addActionListener(this);
        start.addActionListener(this);
        end.addActionListener(this);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 1;
        c.gridy = 10;
        
        pane.add(bUp, c);

        c.gridx++;
        pane.add(bDown, c);

        c.gridx++;
        pane.add(bLeft, c);

        c.gridx++;
        pane.add(bRight, c);

        c.gridx++;
        pane.add(bZoomPlus, c);

        c.gridx++;
        pane.add(bZoomMinus, c);
        
        c.gridx++;
        pane.add(pause, c);
        
        c.gridx++;
        pane.add(start, c);
        
        c.gridx++;
        pane.add(end, c);
    }

    private void addMapToPane(Container pane) {
        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1F;
        c.weighty = 0;
        c.gridheight = 10;
        c.gridwidth = 8;
        
        pane.add(this.mapa, c);
    }
    
    /*
     * CAPTURACIO D'EVENTS
     */
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String str = e.getActionCommand();

        switch(str){
	        case "Z+": this.mapa.zoomIn(0.1f); break;
	        case "Z-": this.mapa.zoomOut(0.1f); break;
	        case "Up": this.mapa.moveDown(); break;
	        case "Down": this.mapa.moveUp(); break;
	        case "<": this.mapa.moveRight(); break;
	        case ">": this.mapa.moveLeft(); break;
	        case "Pause": Aeroport.pauseCity = true; break;
	        case "Start": Aeroport.pauseCity = false; break;
	        case "End": Aeroport.endCity = true; break;
        }
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
        this.mapa.setFactorXY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            this.mapa.zoomIn();
        } else {
            this.mapa.zoomOut();
        }
    }
    
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

	/*
	 * RUTES
	 */
    
	private void crearRutes(){
		//Ruta Oest: Aterra i despega enrrera
		rutaAlFingerOest = new ArrayList<String>();
		rutaAlFingerOest.add(("iniciPista"));
		rutaAlFingerOest.add(("h1"));
		rutaAlFingerOest.add(("v1"));
		rutaAlFingerOest.add(("goFingers"));
		
		rutaDespegueOest = new ArrayList<String>();
		rutaDespegueOest.add(("goFingers"));
		rutaDespegueOest.add(("fiPista"));
		rutaDespegueOest.add(("pista"));
		
		//Ruta Est: Aterra i despega envant
		rutaAlFingerEst = new ArrayList<String>();
		rutaAlFingerEst.add("fiPista");
		rutaAlFingerEst.add("h2");
		rutaAlFingerEst.add("iniciPista");
		rutaAlFingerEst.add(("h1"));
		rutaAlFingerEst.add(("v1"));
		rutaAlFingerEst.add(("goFingers"));
		
		rutaDespegueEst = new ArrayList<String>();
		rutaDespegueEst.add(("goFingers"));
		rutaDespegueEst.add(("fiPista"));
		rutaDespegueEst.add("h2");
		rutaDespegueEst.add("iniciPista");
		rutaDespegueEst.add(("pista"));
		
	}
	
	private synchronized Finger afegirFingerARuta(ArrayList<String> ruta) {
		Finger finger;
		while(!hiHaUnQualqueFinguerBuit()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		finger = getPrimerFingerBuit();
		ruta.add(finger.idWay);

		return finger;
	}

	private ArrayList<String> triarRutaAlFinger(int vent) {
		switch(vent){
		case 0: return rutaAlFingerEst;
		case 1: return rutaAlFingerOest;
		}
		return null;
	}
	
	private ArrayList<String> triarRutaDespegue(int vent) {
		switch(vent){
		case 0: return rutaDespegueEst;
		case 1: return rutaDespegueOest;
		}
		return null;
	}
	
	/*
	 * AVIONS
	 */
	
	public ArrayList<Avio> getAvions(){
		return avions;
	}
	
	private void addAvio(String idavio, Carrer way, Direction direction, ArrayList<String> rutaAlFinger, ArrayList<String> rutaDespegue, Finger finger){
		Avio avio = new Avio(this, idavio, way, direction, finger, rutaAlFinger, rutaDespegue);
		avions.add(avio);
		avio.start();
	}
	
	public void paintAvions(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
		for(int i=0; i < avions.size(); i++){
			avions.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
	}
	
	public void borrarAvio(Avio avio){
		avions.remove(avio);
	}
	
	/*
	 * FINGERS
	 */
	
	private Finger getPrimerFingerBuit(){
		Finger finger;
		for (int i = 0; i < fingers.size(); i++) {
			finger = fingers.get(i);
			if (finger.estaDisponible()) {
				this.canviarEstatFinger(finger, Estat.reservat);
				return fingers.get(i);
			}
		}
		return null;
	}
	
	private boolean hiHaUnQualqueFinguerBuit() {
		for (int i = 0; i < fingers.size(); i++) {
			if (fingers.get(i).estaDisponible()) {
				return true;
			}
		}		
		return false;
	}
	
	public synchronized void canviarEstatFinger(Finger finger, Estat estat){
		for (int i = 0; i < fingers.size(); i++) {
			if (fingers.get(i).equals(finger)) {
				fingers.get(i).setEstat(estat);
			}
		}
		if(estat.equals(Estat.buit)) notify();
	}
	
	/*
	 * VENT
	 */
	
	public Direction direccioDespegue(){
		if(mapa.getVent() == 0) return Direction.FORWARD;
		else if(mapa.getVent() == 1) return Direction.BACKWARD;
		
		return null;
	}
	
	/*
	 * RUN 
	 */
    
    @SuppressWarnings("unchecked")
    public void run() {
    	Direction direccio = null;
        int i = 0, vent;
        
        while (!Aeroport.isEnd()) {
            if (!Aeroport.isPaused() && avions.size() < MAX_AVIONS) {
            	vent = mapa.getVent();
        		if(vent == 0) direccio = Direction.FORWARD;
        		else if(vent ==1) direccio = Direction.BACKWARD;
        				
        		ArrayList<String> rutaAlFinger = triarRutaAlFinger(vent);
        		ArrayList<String> rutaDespegue = triarRutaDespegue(vent);
        		Finger finger = afegirFingerARuta(rutaAlFinger);
        			
        		try {
        			addAvio("A10"+i, carrers.get(0), direccio, (ArrayList<String>)(rutaAlFinger.clone()), (ArrayList<String>)rutaDespegue.clone(), finger);
        			rutaAlFinger.remove(rutaAlFinger.size()-1);
        			Thread.sleep(2000);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        		i++;
            }
        }
        finalitzar();
    }

    /*
     * CONTROL AEROPORT
     */
    
    private void finalitzar() {
    	try {
	        avions.removeAll(avions);
	        mapa.paint();
			Thread.sleep(100);
			this.dispose();
    	} catch (InterruptedException e) {
		}
	}

	public static boolean isPaused() {
        return Aeroport.pauseCity;
    }
    
    public static boolean isEnd() {
        return Aeroport.endCity;
    }
}