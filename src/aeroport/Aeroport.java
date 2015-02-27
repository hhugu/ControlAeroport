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

import ciutat.ControlTrafic;
import aeroport.Finger.EstatFinguer;
import aeroport.avions.Avio;
import aeroport.avions.Avio.Direction;
import aeroport.avions.AvioComercial;
import aeroport.avions.Caza;


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

    private static volatile boolean pauseCity;
    private static volatile boolean endCity;
        
    ArrayList <Avio> avions = new ArrayList<Avio>();
	private ArrayList <Carrer> pistes;
	private ArrayList <Finger> fingers;
	private ArrayList <String> rutaAlFingerOest, rutaDespegueOest, rutaAlFingerEst, rutaDespegueEst;
	
    private Mapa mapa;
    private ControlTrafic controladorTrafic;
    
    public Aeroport() {
    	setDefaultCloseOperation(EXIT_ON_CLOSE);
    	
        mapa = new Mapa(Aeroport.CIUTAT_CM_WIDTH, Aeroport.CIUTAT_CM_HEIGHT, Aeroport.MAPA_PIX_WIDTH, Aeroport.MAPA_PIX_HEIGH, this);
        controladorTrafic = new ControlTrafic(mapa);
        mapa.setControltrafic(controladorTrafic);

        pistes = mapa.getPistes();
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
    
    /**
     * Crea totes les posibles rutes per anar cap als fingers i per anar cap a la pista.
     * Despres s'escolligar en funcio del vent.
     * @author hugu
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
		rutaAlFingerEst.add(("goFingers"));
		
		rutaDespegueEst = new ArrayList<String>();
		rutaDespegueEst.add(("goFingers"));
		rutaDespegueEst.add(("v1"));
		rutaDespegueEst.add("h1");
		rutaDespegueEst.add("iniciPista");
		rutaDespegueEst.add(("pista"));
		
	}

	/**
	 * Retorna la ruta fins al figuer a seguir per l'avio en funcio del fent
	 * @param vent - int
	 * @return ArrayList<String>
	 */
	private ArrayList<String> triarRutaAlFinger(int vent) {
		switch(vent){
		case 0: return rutaAlFingerEst;
		case 1: return rutaAlFingerOest;
		}
		return null;
	}
	
	/**
	 * Retorna la ruta fins a la pista a seguir per l'avio en funcio del fent
	 * @param vent - int
	 * @return ArrayList<String>
	 */
	private ArrayList<String> triarRutaDespegue(int vent) {
		switch(vent){
		case 0: return rutaDespegueEst;
		case 1: return rutaDespegueOest;
		}
		return null;
	}
	
	/**
	 * Afegeix el finger a la ruta. Si estan tots ocupats es queda esperant a que li notifiquin que hi ha un 
	 * finger disposible.
	 * @param ruta - ArrayList<String>
	 * @return fingerDisponible - Finger
	 */
	private synchronized Finger afegirFingerARuta(ArrayList<String> ruta) {
		Finger fingerDisponible;
		while(!hiHaUnQualqueFinguerBuit()){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		fingerDisponible = getPrimerFingerBuit();
		ruta.add(fingerDisponible.idWay);

		return fingerDisponible;
	}
	
	/*
	 * FINGERS
	 */
	
	/**
	 * Helper per obtenir el primer finger buit. Un cop obtes el reserva per l'avio.
	 * @return - fingerDisponible - Finger
	 */
	private Finger getPrimerFingerBuit(){
		Finger fingerDisponible;
		for (int i = 0; i < fingers.size(); i++) {
			fingerDisponible = fingers.get(i);
			if (fingerDisponible.estaDisponible()) {
				this.canviarEstatFinger(fingerDisponible, EstatFinguer.reservat);
				return fingers.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Helper per sabre si hi ha un finger disponible.
	 * @return boolean - True si n'hi ha qualqun, False si no. 
	 */
	private boolean hiHaUnQualqueFinguerBuit() {
		for (int i = 0; i < fingers.size(); i++) {
			if (fingers.get(i).estaDisponible()) {
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * Helper per canviar l'estat a un figuer. Si el finguer es buida notifica a qui espera que n'hi ha un de buit.
	 * @param finger - Finger
	 * @param estat - EstatFinger
	 */
	public synchronized void canviarEstatFinger(Finger finger, EstatFinguer estat){
		for (int i = 0; i < fingers.size(); i++) {
			if (fingers.get(i).equals(finger)) {
				fingers.get(i).setEstat(estat);
			}
		}
		if(estat.equals(EstatFinguer.buit)) notify();
		if(estat.equals(EstatFinguer.ocupat)) controladorTrafic.addCotxe();
	}
	
	/*
	 * AVIONS
	 */
	
	public ArrayList<Avio> getAvions(){
		return avions;
	}
	
	/**
	 * Helper per crear un avio i posarlo en marxa.
	 * @param idavio - String
	 * @param way - Carrer
	 * @param direction - Direcction
	 * @param rutaAlFinger - ArrayList<String>
	 * @param rutaDespegue - ArrayList<String>
	 * @param finger - Finger
	 */
	private void addAvio(String idavio, Carrer way, Direction direction, ArrayList<String> rutaAlFinger, ArrayList<String> rutaDespegue, Finger finger){
		int random = (int)(Math.random()*5);
		
		if(random == 0 || random == 4){
			Avio avio = new Avio(this, idavio, way, direction, finger, rutaAlFinger, rutaDespegue);
			avions.add(avio);
			(avio).start();
		}else if(random == 1 || random == 3){
			AvioComercial avioComercial = new AvioComercial(this, idavio, way, direction, finger, rutaAlFinger, rutaDespegue);
			avions.add(avioComercial);
			(avioComercial).start();
		}else if(random == 2){
			Caza caza = new Caza(this, idavio, way, direction, finger, rutaAlFinger, rutaDespegue);
			avions.add(caza);
			(caza).start();
		}
	}
	
	/**
	 * Helper que crida al paint de l'avio de tots els avions dins l'arrayList d'avions.
	 * @param g - Graphics
	 * @param factorX - float
	 * @param factorY - float
	 * @param offsetX - int
	 * @param offsetY - int
	 */
	public void paintAvions(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
		for(int i=0; i < avions.size(); i++){
			if (avions.get(i).getEstat() != null) avions.get(i).paint(g, factorX, factorY, offsetX, offsetY);
		}
	}
	
	/**
	 * Helper per borrar l'avio de l'ArrayList d'avions
	 * @param avio
	 */
	public void borrarAvio(Avio avio){
		avions.remove(avio);
	}
	
	/*
	 * VENT
	 */
	
	/**
	 * Metode per obtenir la direccio en funcio del vent
	 * @return Direcction
	 */
	public Direction direccioDespegue(){
		if(mapa.getVent() == 0) return Direction.FORWARD;
		else if(mapa.getVent() == 1) return Direction.BACKWARD;
		
		return null;
	}
	
	 /*
     * CONTROL AEROPORT
     */
    
	/**
	 * Metode per sabre si si quan avanci l'avio tocara a un altre.
	 * @param avioQueDemanaPermis - Avio
	 * @return boolean - True si pot avançar, false si tocara a cualque avio.
	 */
	public boolean pucAvançar(Avio avioQueDemanaPermis){
		Avio avioQueSeMou;
		for (int i = 0; i < avions.size(); i++) {
			avioQueSeMou = avions.get(i);
			if (!avioQueDemanaPermis.equals(avioQueSeMou) && avioQueSeMou.getEstat() != null) {
				if(avioQueDemanaPermis.posicioQueOcupare().intersects(avioQueSeMou.posicioQueOcup())) return false;
			}
		}
		return true;
	}
    
    private void finalitzar() {
    	try {
	        avions.removeAll(avions);
	        mapa.paint();
			Thread.sleep(100);
			dispose();
    	} catch (InterruptedException e) {
		}
	}

	public static boolean isPaused() {
        return Aeroport.pauseCity;
    }
    
    public static boolean isEnd() {
        return Aeroport.endCity;
    }
    
	/*
	 * RUN 
	 */
    
    @SuppressWarnings("unchecked")
    public void run() {
    	Direction direccio = null;
        int i = 0, vent;
        
        while (!Aeroport.isEnd()) {
            if (!Aeroport.isPaused() && (avions.size() < MAX_AVIONS)) {
            	vent = mapa.getVent();
        		if(vent == 0) direccio = Direction.FORWARD;
        		else if(vent ==1) direccio = Direction.BACKWARD;
        				
        		ArrayList<String> rutaAlFinger = triarRutaAlFinger(vent);
        		ArrayList<String> rutaDespegue = triarRutaDespegue(vent);
        		
        		Finger finger = afegirFingerARuta(rutaAlFinger);
        			
        		try {
        			addAvio("A10"+i, pistes.get(0), direccio, (ArrayList<String>)(rutaAlFinger.clone()), (ArrayList<String>)rutaDespegue.clone(), finger);
        			rutaAlFinger.remove(rutaAlFinger.size()-1);
        			Thread.sleep(1000);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        		i++;
            }
        }
        finalitzar();
    }
    
    public Carrer getCarrerCritic(){
    	for (int i = 0; i < pistes.size(); i++) {
    		if (pistes.get(i).getId().equals("fiPista")) return pistes.get(i);
		}
    	return null;
    }

}