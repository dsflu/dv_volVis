/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

import java.io.File;
import java.io.IOException;
import util.VectorMath;

/**
 *
 * @author michel
 * @Anna 
 * Volume object: This class contains the object and assumes that the distance between the voxels in x,y and z are 1 
 */
public class Volume {
    
    public Volume(int xd, int yd, int zd) {
        data = new short[xd*yd*zd];
        dimX = xd;
        dimY = yd;
        dimZ = zd;
    }
    
    public Volume(File file) {
        
        try {
            VolumeIO reader = new VolumeIO(file);
            dimX = reader.getXDim();
            dimY = reader.getYDim();
            dimZ = reader.getZDim();
            data = reader.getData().clone();
            computeHistogram();
        } catch (IOException ex) {
            System.out.println("IO exception");
        }
        
    }
    
    
    public short getVoxel(int x, int y, int z) {
        return data[x + dimX*(y + dimY * z)];
       
    }
    
    public void setVoxel(int x, int y, int z, short value) {
        data[x + dimX*(y + dimY*z)] = value;
    }

    public void setVoxel(int i, short value) {
        data[i] = value;
    }

    
    /*
     * do the same thing as Math.floor(), but return an int value
     * @param value
     * @return int floor value
     */
    private static int floor( double value ){
        return (int) Math.floor(value);
    }
    
    public short getVoxelInterpolate(double[] coord) {
    /* to be implemented: get the trilinear interpolated value. 
        The current implementation gets the Nearest Neightbour */
        
        if (coord[0] < 0 || coord[0] > (dimX-2) || coord[1] < 0 || coord[1] > (dimY-2)
                || coord[2] < 0 || coord[2] > (dimZ-2)) {
            return 0;
        }
        /* notice that in this framework we assume that the distance between neighbouring voxels is 1 in all directions*/
        // int x = (int) Math.round(coord[0]); 
        // int y = (int) Math.round(coord[1]);
        // int z = (int) Math.round(coord[2]);
    
        // return getVoxel(x, y, z);
        //int vertexCoord[][] = new int[8][3];
        int colorValues[] = new int[8];
        double tempcolor[] = new double[7];
        int p = 0;


        int x_f = floor( coord[0] );
        int y_f = floor( coord[1] );
        int z_f = floor( coord[2] );

        int x_u = x_f+1 > dimX ? x_f : x_f+1;
        int y_u = y_f+1 > dimY ? y_f : y_f+1;
        int z_u = z_f+1 > dimZ ? z_f : z_f+1;

        double xd = coord[0]-x_f;
        double yd = coord[1]-y_f;
        double zd = coord[2]-z_f;
        double xd2 = x_u-coord[0];
        double yd2 = y_u-coord[1];
        double zd2 = z_u-coord[2];

        colorValues[0] = getVoxel( x_f,y_f,z_f);
        colorValues[1] = getVoxel( x_f,y_f,z_u);
        colorValues[2] = getVoxel( x_f,y_u,z_f);
        colorValues[3] = getVoxel( x_f,y_u,z_u);
        colorValues[4] = getVoxel( x_u,y_f,z_f);
        colorValues[5] = getVoxel( x_u,y_f,z_u);
        colorValues[6] = getVoxel( x_u,y_u,z_f);
        colorValues[7] = getVoxel( x_u,y_u,z_u);

        tempcolor[0] = colorValues[0]*zd2 + colorValues[1]*zd;
        tempcolor[1] = colorValues[2]*zd2 + colorValues[3]*zd;
        tempcolor[2] = tempcolor[0]*yd2 + tempcolor[1]*yd;

        tempcolor[3] = colorValues[4]*zd2 + colorValues[5]*zd;
        tempcolor[4] = colorValues[6]*zd2 + colorValues[7]*zd;
        tempcolor[5] = tempcolor[3]*yd2 + tempcolor[4]*yd;

        tempcolor[6] =tempcolor[2]*xd2+tempcolor[5]*xd;
        tempcolor[6] = (short) Math.round(tempcolor[6]);
        
        if ( tempcolor[6] > 255 ){
            //System.err.println("err color:" + tempcolor[6]  );
            tempcolor[6] = 255;
        }
        
        return (short) tempcolor[6];
    }
    
    public short getVoxel(int i) {
        return data[i];
    }
    
    public int getDimX() {
        return dimX;
    }
    
    public int getDimY() {
        return dimY;
    }
    
    public int getDimZ() {
        return dimZ;
    }

    public short getMinimum() {
        short minimum = data[0];
        for (int i=0; i<data.length; i++) {
            minimum = data[i] < minimum ? data[i] : minimum;
        }
        return minimum;
    }

    public short getMaximum() {
        short maximum = data[0];
        for (int i=0; i<data.length; i++) {
            maximum = data[i] > maximum ? data[i] : maximum;
        }
        return maximum;
    }
 
    public int[] getHistogram() {
        return histogram;
    }
    
    private void computeHistogram() {
        histogram = new int[getMaximum() + 1];
        for (int i=0; i<data.length; i++) {
            histogram[data[i]]++;
        }
    }
    
    private int dimX, dimY, dimZ;
    private short[] data;
    private int[] histogram;
}