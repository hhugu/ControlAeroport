//package aeroport;
//
//import java.awt.Graphics;
//import java.awt.Image;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import javax.imageio.ImageIO;
//
//import aeroport.Finger.Estat;
//
//public class Avio extends Thread {
//
//    private int cmLong, cmWidth, speed, posicio = 0;
//    private volatile int cmPosition;
//    private Image imgAvioH, imgAvioV;
//    private volatile Carrer way;
//    private Finger finger;
//    private String idAvio;
//    private Direction direction;
//    private CrossRoad proximCruce = null;
//    private ArrayList<Carrer> rutaAlFinger, rutaDespegue;
//    private EstatAvio estat;
//    
//    public Avio(String idAvio, Carrer way, Direction direction, Finger finger, ArrayList<Carrer> rutaAlFinger, ArrayList<Carrer> rutaDespegue) {
//        this.idAvio = idAvio;
//        try {
//			imgAvioH = ImageIO.read(new File("Imagenes/avioH.png"));
//			imgAvioV = ImageIO.read(new File("Imagenes/avioV.png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//        cmLong = 1000;
//        cmWidth = 1000;
//        speed = 10;
//        
//        this.finger = finger;
//        this.rutaAlFinger = rutaAlFinger;
//        this.rutaDespegue = rutaDespegue;
//        
//        cmPosition = 0;
//
//        setWay(way);
//        setDirection(direction);
//    }
//	
//	public void run(){
//		cmPosition = way.getEntryPoint(getDirection());
//		estat = EstatAvio.GOFINGER;
//		while(true){
//			if (!Aeroport.isPaused()) {
//				try {
//					Thread.sleep(10);
//					
//					
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				if (direction == Direction.BACKWARD) cmPosition -= speed;
//				else if(direction == Direction.FORWARD) cmPosition += speed;
//				
//				if(cmPosition == way.getCmLong()) this.direction = Direction.BACKWARD;
//				if(cmPosition == 0) this.direction = Direction.FORWARD;
//				
//				if(estat == EstatAvio.GOFINGER) calcularProximCarrer(rutaAlFinger);
//				else if(estat == EstatAvio.GOPISTA) calcularProximCarrer(rutaDespegue);
//				
//				if(way instanceof Finger){
//					try {
//						finger.setEstat(Estat.ocupat);
//						Thread.sleep(5000);
//						estat = EstatAvio.GOPISTA;
//						finger.setEstat(Estat.buit);
//						posicio = 0;
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
//	
//	public synchronized void paint(Graphics g, float factorX, float factorY, int offsetX, int offsetY) {
//		int iniX, iniY, finX, finY;
//		
//   		finY=(int)(((this.cmWidth)/factorY));
//   		finX=(int)(((this.cmLong)/factorX));
//   		
//   		if(way instanceof HCarrer){
//   			
//   			iniY=(int)((((this.way.cmFinY+this.way.cmIniY)/2-400)/factorY)+offsetY);
//   	   		iniX=(int)((((this.way.cmIniX+this.cmPosition))/factorX)+offsetX);
//   	   		
//	   		if (direction==Direction.BACKWARD) {
//	   			g.drawImage(this.imgAvioH, iniX, iniY, -finX, finY, null);
//			}
//			else if(direction==Direction.FORWARD){
//				g.drawImage(this.imgAvioH, iniX, iniY, finX, finY, null);
//			}
//   		}else if(way instanceof VCarrer || way instanceof Finger){
//   			
//   			iniY=(int)((((this.way.cmIniY+this.cmPosition))/factorY)+offsetY);
//   	   		iniX=(int)((((this.way.cmIniX+this.way.cmFinX)/2-400)/factorX)+offsetX);
//   	   		
//   			if (direction==Direction.BACKWARD) {
//	   			g.drawImage(this.imgAvioV, iniX, iniY, finY, -finX, null);
//			}
//			else if(direction==Direction.FORWARD){
//				g.drawImage(this.imgAvioV, iniX, iniY, finY, finX, null);
//			}
//   		}
//    }
//	
//    private void calcularProximCarrer(ArrayList<Carrer> ruta) {
//		if (way.insideAnyCrossRoad(cmPosition)) {
//			proximCruce = way.intersectedCrossRoad(cmPosition);
//			System.out.println("*********************\n"+proximCruce.getId());
//			if (posicio < ruta.size()) {
//				if (proximCruce.getCarrer(way).equals(ruta.get(posicio))) {
//					way = proximCruce.getCarrer(way);
//					cmPosition = way.getEntryPoint(getDirection());
//					System.out.println("Carrer acutual: " + way.idWay);
//					posicio++;
//				}
//			}
//		}
//	
//	}
//    
//	public static enum EstatAvio {ATERRANT, GOFINGER, ATURAT, GOPISTA, DESPEGANT};
//
//    public static enum Direction {
//
//        FORWARD(1), BACKWARD(-1);
//        private int increment;
//
//        private Direction(int increment) {
//            this.increment = increment;
//        }
//
//        public int getIncrement() {
//            return this.increment;
//        }
//    }
//    
//    public static enum Orientation {
//    	
//        NORTH(Direction.BACKWARD),
//        SOUDTH(Direction.FORWARD),
//        WEST(Direction.BACKWARD),
//        EAST(Direction.FORWARD);
//        Direction direction;
//
//        private Orientation(Direction direction) {
//            this.direction = direction;
//        }
//
//        public static Direction getDirection(Orientation orientation) {
//            if ((orientation == Orientation.SOUDTH) || (orientation == Orientation.EAST)) {
//                return Direction.FORWARD; // =================================>>
//            }
//            return Direction.BACKWARD;
//        }
//
//        public static Orientation getOrientation(Avio avio) {
//            return getOrientation(avio.getWay(), avio.getDirection());
//        }
//
//        public static Orientation getOrientation(Carrer way, Direction direction) {
//            if (way instanceof VCarrer) {
//                if (direction == Direction.FORWARD) {
//                    return Orientation.SOUDTH; //=============================>>
//                } else {
//                    return Orientation.NORTH; //==============================>>
//                }
//            }
//
//            if (way instanceof HCarrer) {
//                if (direction == Direction.FORWARD) {
//                    return Orientation.EAST; //===============================>>
//                } else {
//                    return Orientation.WEST; //===============================>>
//                }
//            }
//
//            return Orientation.WEST;
//        }
//
//        public static int getDegrees(Orientation orientation) {
//            switch (orientation) {
//                case NORTH:
//                    return 0;
//                case EAST:
//                    return 90;
//                case SOUDTH:
//                    return 180;
//                case WEST:
//                    return 270;
//            }
//
//            return 0;
//        }
//    }
//    public Direction getDirection() {
//		return direction;
//	}
//
//	public Carrer getWay() {
//		return this.way;
//	}
//
//	public int getCmPosition() {
//	        return this.cmPosition;
//	}
//
//	public int getLongInCm() {
//		return this.cmLong;
//	}
//
//	public int getSpeed() {
//		return speed;
//	}
//
//	public void setSpeed(int speed) {
//		this.speed = speed;
//	}
//
//	public String getIdCar() {
//		return idAvio;
//	}
//
//	public void setCmPosition(int cmPosition) {
//		this.cmPosition = cmPosition;
//	}
//
//	public void setWay(Carrer way) {
//		this.way = way;
//	}
//
//	public void setDirection(Direction direction) {
//		this.direction = direction;
//	}
//
//	public int getWidthInCm() {
//		return this.cmWidth;
//	}
//
//	public void setPositionInCm(int cmPos) {
//        this.cmPosition = cmPos;
//		
//	}
//
//}