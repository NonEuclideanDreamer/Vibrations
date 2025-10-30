//***************************************************************************************************************
// Author: Non-Euclidean Dreamer
// Solving the Wave Equation on a 2d Surface
//****************************************************************************************************************

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import javax.imageio.ImageIO;

public class Surface 
{
	static String name="vibe";
	static int width=1080,height=1080,start=0;
	static BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
	static double k=0.5, //wave speed, best left 0.5, otherwise dampening breaks
			delt=1,//time step per iteration, must be <=1
			colorwidth=3*256; //mapping excitation to color
	double[][][]state;
	static double[][][]zero=new double[height][2][4];//if we dampen at the boundary
	static int t;
	static DecimalFormat df=new DecimalFormat("0000");
	static Random rand=new Random();
	static int[]boundmode= {0,0,0,0};//0=toroidal,1=mirror, 2=fixed, 3=dampen,4=zero
	public static void main(String[]args)
	{
		Surface surface=new Surface();
		int x=width/2,
				y=height/2,
				c=0,
				k=8;

		double w=2*Math.PI/width*k;
		for(int i=0;i<1440;i++)
		{
			
		
		for(int j=0;j<3;j++)//how many steps to iterate before drawing an image
		{
		//	for(int n=0;n<100;n++)//for wavefront
			{
				surface.state[x][y][2]=Math.cos(c*w)*w;surface.state[x][y][t%2]=Math.sin(c*w);
			}
			c++;
			surface.step();
		}
		
			surface.draw();
			print(name+df.format(i+start)+".png");
		}
	}
	
	public Surface()
	{
		state=new double[width][height][3];
	
		t=0;
	}
	
	public void step()
	{
		for(int j=0;j<4;j++)if(boundmode[j]==3) {int limit=width; if (j%2==0) limit=height;
		for(int i=0;i<limit;i++)
		{if(j%2==0)
			zero[i][(t+1)%2][j]=state[j/2*(width-1)][i][(t+1)%2];
		else
			zero[i][(t+1)%2][j]=state[i][j/2*(height-1)][(t+1)%2];
		}
		}
		for(int i=0;i<width;i++)for(int j=0;j<height;j++)
		{
			double r=Math.sqrt(VG.norm2(i-width/2,j-height/2));
			if(r%200<100||(i<width/2+50&&i>width/2-50&&(j+400)%500<250))	
			{
				state[i][j][2]+=force(i,j);
				state[i][j][(t+1)%2]=state[i][j][t%2]+state[i][j][2]*delt;
			}
		}
			t++;
	}
	
	public void draw()
	{
		for(int i=0;i<width;i++)for(int j=0;j<height;j++)
		{
			image.setRGB(i, j, spectrum(4*256+(int)(state[i][j][t%2]*colorwidth),1,1));
		}
	}
	
	private static void print(String n) 
	{
		File outputfile = new File(n);
		try {  
			ImageIO.write(image,"png", outputfile);
			System.out.println(n+" finished.");
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
		}
	}
	
	public void dampen()
	{
		
	}
	
	public double force(int i,int j)//Modify Wave speed in certain areas...
	{
	//	double k=this.k;
	//	if(inCircle(i,j,width/2,height/2,height/9))k=1.0/16;else if(inCircle(i,j,width/2,height/2,height*2/9))k=1.0/8;else if(inCircle(i,j,(width/2),height/2,height/3))k=.25;//k*=Math.pow(.9999, t);
		double xp=nb(i,j,2),x=state[i][j][t%2],xm=nb(i,j,0),yp=nb(i,j,1),ym=nb(i,j,3);
		//if(i==321&&j==720)System.out.println("force="+k*k*(xp+xm+yp+ym-4*x));
		return k*k*(xp+xm+yp+ym-4*x);
	}
	
	private boolean inCircle(int i, int j, int xm, int ym, int r) {
		
		return r*r>(i-xm)*(i-xm)+(j-ym)*(j-ym);
	}

	static int[][] nb= {{-1,0},{0,-1},{1,0},{0,1}};
	
	double nb(int i,int j,int k)
	{
		if(boundmode[k]==1)
		{
			if(i+nb[k][0]==-1)return state[1][j][t%2];
			if(i+nb[k][0]==width)return state[width-2][j][t%2];
			if(j+nb[k][1]==-1)return state[i][1][t%2];
			if(j+nb[k][1]==height)return state[i][height-2][t%2];}
		if(boundmode[k]==2) {
		if(i+nb[k][0]==-1)return state[i][j][t%2];
		if(i+nb[k][0]==width)return state[width-1][j][t%2];
		if(j+nb[k][1]==-1)return state[i][0][t%2];
		if(j+nb[k][1]==height)return state[i][height-1][t%2];}
		if(boundmode[k]==3){
			if(i+nb[k][0]==-1)return zero[j][t%2][k];
			if(i+nb[k][0]==width)return zero[j][t%2][k];
			if(j+nb[k][1]==-1)return zero[i][t%2][k];
			if(j+nb[k][1]==height)return zero[i][t%2][k];}
		if(boundmode[k]==4){
			if(i+nb[k][0]==-1)return 0;
			if(i+nb[k][0]==width)return 0;
			if(j+nb[k][1]==-1)return 0;
			if(j+nb[k][1]==height)return 0;}
		return state[(i+nb[k][0]+width)%width][(j+nb[k][1]+height)%height][t%2];
	}
	
	private boolean slit(int i) {
		int size0=Math.min(t/200,100),size1=(t-2000)/200;
		return ((i>height/2-size0&&i<height/2+size0)||(i>height/2+200-size1&&i<height/2+200+size1));
	}

	public static int spectrum(int n, double d, double l)
	{
		double full=d*l, term=(1-l)*(1+d)*255/2;
		n=(n%(6*256)+6*256)%(6*256);
		if (n<256)
			return new Color((int) (255*full+term),(int) (n*full+term),(int) term).getRGB();
		n-=256;
		if (n<256)
			return new Color((int) ((255-n)*full+term),(int) (255*full+term),(int) term).getRGB();
		n-=256;
		if (n<256)
			return new Color((int) term,(int) (255*full+term),(int) (n*full+term)).getRGB();
		n-=256;
		if (n<256)
			return new Color((int) term,(int) ((255-n)*full+term),(int) (255*full+term)).getRGB();
		n-=256;
		if (n<256)
			return new Color((int) (n*full+term),(int) term,(int) (255*full+term)).getRGB();
		n-=256;
			return new Color((int) (255*full+term),(int) term,(int) ((255-n)*full+term)).getRGB();
	}
}
