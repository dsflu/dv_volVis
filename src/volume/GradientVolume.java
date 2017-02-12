/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;


import static java.lang.Math.sqrt;
import java.util.ArrayList;
import util.VectorMath;
import volvis.TransferFunction;
import volume.VoxelGradient;


/**
 *
 * @author michel
 * @ Anna
 * This class contains the pre-computes gradients of the volume. This means calculates the gradient
 * at all voxel positions, and provides functions
 * to get the gradient at any position in the volume also continuous..
*/
public class GradientVolume {

    public GradientVolume(Volume vol) {
        volume = vol;
        dimX = vol.getDimX();
        dimY = vol.getDimY();
        dimZ = vol.getDimZ();
        data = new VoxelGradient[dimX * dimY * dimZ];
        
        compute();
        maxmag = -1.0;
    }

    public VoxelGradient getGradient(int x, int y, int z) {
        return data[x + dimX * (y + dimY * z)];
    }

    private void interpolate(VoxelGradient g0, VoxelGradient g1, float factor, VoxelGradient result) {
        /* To be implemented: this function linearly interpolates gradient vector g0 and g1 given the factor (t) 
            the resut is given at result. You can use it to tri-linearly interpolate the gradient */
        //result.mag=g0.mag*(1-factor)+g1.mag*factor;
        result.x=g0.x*(1-factor)+g1.x*factor;//interpolate gradient in x, y, z direction
        result.y=g0.y*(1-factor)+g1.y*factor;
        result.z=g0.z*(1-factor)+g1.z*factor;
        result.mag=(float)Math.sqrt( result.x*result.x+result.y*result.y+result.z*result.z);//compute the magnitude of the interpolated gradient
        
    }
    
    public VoxelGradient getGradientNN(double[] coord) {
        /* Nearest neighbour interpolation applied to provide the gradient */
        if (coord[0] < 0 || coord[0] > (dimX-2) || coord[1] < 0 || coord[1] > (dimY-2)
                || coord[2] < 0 || coord[2] > (dimZ-2)) {
            return zero;
        }

        int x = (int) Math.round(coord[0]);
        int y = (int) Math.round(coord[1]);
        int z = (int) Math.round(coord[2]);

        
        return getGradient(x, y, z);
    }

    
    public VoxelGradient getGradient(double[] coord) {
    /* To be implemented: Returns trilinear interpolated gradient based on the precomputed gradients. 
     *   Use function interpolate. Use getGradientNN as bases */
   //     TransferFunction tFunc = new TransferFunction(volume.getMinimum(), volume.getMaximum());
        //private TransferFunction tf = new TransferFunction(); 
        // List<float> result= ne ArrayList<float>();
         if (coord[0] < 0 || coord[0] > (dimX-2) || coord[1] < 0 || coord[1] > (dimY-2)
                || coord[2] < 0 || coord[2] > (dimZ-2)) {
            return zero;
        }
        int x = (int) Math.floor(coord[0]); //Find one vertex in the cube to look up other vertices
        int y = (int) Math.floor(coord[1]);
        int z = (int) Math.floor(coord[2]);

            if  (x<=(dimX-2)&& y<=(dimY-2) && z<=(dimZ-2)){
            VoxelGradient result_z1=new VoxelGradient();
            VoxelGradient result_z2=new VoxelGradient();
            interpolate(getGradient(x,y,z),getGradient(x,y,z+1), (float) (coord[2]-z),result_z1);
            interpolate(getGradient(x,y+1,z),getGradient(x,y+1,z+1), (float) (coord[2]-z),result_z2);
            VoxelGradient result_z3=new VoxelGradient();
            VoxelGradient result_z4=new VoxelGradient();
            interpolate(getGradient(x+1,y,z),getGradient(x+1,y,z+1), (float) (coord[2]-z),result_z3);
            interpolate(getGradient(x+1,y+1,z),getGradient(x+1,y+1,z+1), (float) (coord[2]-z),result_z4);
            VoxelGradient result_y1=new VoxelGradient();
            VoxelGradient result_y2=new VoxelGradient();
            interpolate(result_z1,result_z2, (float) (coord[1]-y),result_y1);
            interpolate(result_z3,result_z4, (float) (coord[1]-y),result_y2);
            VoxelGradient result_x=new VoxelGradient();
            interpolate(result_y1,result_y2, (float) (coord[0]-x),result_x);    //user trilinear interpolation to compute the gradients 

           
            return result_x;     
            }
            else{
                getGradientNN(coord);
            return getGradient(0,0,0);
            }
   
    //getGradientNN(coord);
    }
    
  
    public void setGradient(int x, int y, int z, VoxelGradient value) {
        data[x + dimX * (y + dimY * z)] = value;
    }

    public void setVoxel(int i, VoxelGradient value) {
        data[i] = value;
    }

    public VoxelGradient getVoxel(int i) {
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

    private void compute() {
        /* To be implemented: compute the gradient of contained in the volume attribute */
        int px0,py0,pz0,px1,py1,pz1;
        int n=0;
        for (int i=0; i<data.length; i++) {            
            data[i]=zero;
        }

            for (int x=0; x<dimX-1; x++){
                for (int y=0; y<dimY-1; y++){
                    for (int z=0; z<dimZ-1; z++){
            if (x==0){                                  
                px0=volume.getVoxel(x, y, z);                   //change the lower (x-1,y,z) to (x,y,z)
            }
            else{ px0=volume.getVoxel(x-1, y, z);}
            if (x==dimX-1){
                px1=volume.getVoxel(x, y, z);                   //change the upper (x+1,y,z) to (x,y,z)
            }
            else{ px1=volume.getVoxel(x+1, y, z);}
            if (y==0){
                py0=volume.getVoxel(x, y, z);                    //change the lower (x,y-1,z) to (x,y,z)
            }
            else{ py0=volume.getVoxel(x, y-1, z);}
            if (y==dimY-1){
                py1=volume.getVoxel(x, y, z);                    //change the lower (x,y+1,z) to (x,y,z)
            }
            else{ py1=volume.getVoxel(x, y+1, z);}
            
            if (z==0){
                pz0=volume.getVoxel(x, y, z);                    //change the lower (x,y,z-1) to (x,y,z)
            }
            else{ pz0=volume.getVoxel(x, y, z-1);}
            if (z==dimZ-1){
                pz1=volume.getVoxel(x, y, z);                    //change the upper (x,y,z+1) to (x,y,z)
            }
            else{ pz1=volume.getVoxel(x, y, z+1);}
            
            zero.x=(px1-px0)/2;
            zero.y=(py1-py0)/2;
            zero.z=(pz1-pz0)/2;
            data[x + dimX * (y + dimY * z)]=new VoxelGradient(zero.x,zero.y,zero.z);
            }
        }   
    }
    }
    public double getMaxGradientMagnitude() {
        /* to be implemented: Returns the maximum gradient magnitude*/
        for (VoxelGradient data1 : data) {
            if (maxmag<data1.mag) {
                maxmag = data1.mag;
            }
        }
       
        return maxmag;
        
    }
    
    private int dimX, dimY, dimZ;
    private VoxelGradient zero = new VoxelGradient();
    VoxelGradient[] data;
    Volume volume;
    double maxmag;

//    private int mod(int i) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    
    
}
