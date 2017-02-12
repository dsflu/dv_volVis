/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gui.RaycastRendererPanel;
import gui.TransferFunction2DEditor;
import gui.TransferFunctionEditor;
import java.awt.image.BufferedImage;
import util.TFChangeListener;
import util.VectorMath;
import volume.GradientVolume;
import volume.Volume;
import volume.VoxelGradient;
//import java.io.*;
//import static java.lang.System.out;

/**
 *
 * @author michel
 * @Anna
 * This class has the main code that generates the raycasting result image. 
 * The connection with the interface is already given.  
 * The different modes mipMode, slicerMode, etc. are already correctly updated
 */
public class RaycastRenderer extends Renderer implements TFChangeListener {

    private Volume volume = null;
    private GradientVolume gradients = null;
    RaycastRendererPanel panel;
    TransferFunction tFunc;
    TransferFunctionEditor tfEditor;
    TransferFunction2DEditor tfEditor2D;
    private boolean mipMode = false;
    private boolean slicerMode = true;
    private boolean compositingMode = false;
    private boolean tf2dMode = false;
    private boolean shadingMode = false;
    private boolean AmbMode = false;
    private boolean DifMode = false;
    private boolean SpecMode = false;
    private double sampleStep = 1.0;
    int testi =1;
    int testii =1;
    
    public RaycastRenderer() {
        panel = new RaycastRendererPanel(this);
        panel.setSpeedLabel("0");
    }

    public void setVolume(Volume vol) {
        System.out.println("Assigning volume");
        volume = vol;

        System.out.println("Computing gradients");
        gradients = new GradientVolume(vol);

        // set up image for storing the resulting rendering
        // the image width and height are equal to the length of the volume diagonal
        int imageSize = (int) Math.floor(Math.sqrt(vol.getDimX() * vol.getDimX() + vol.getDimY() * vol.getDimY()
                + vol.getDimZ() * vol.getDimZ()));
        if (imageSize % 2 != 0) {
            imageSize = imageSize + 1;
        }
        image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        tFunc = new TransferFunction(volume.getMinimum(), volume.getMaximum());
        tFunc.setTestFunc();
        tFunc.addTFChangeListener(this);
        tfEditor = new TransferFunctionEditor(tFunc, volume.getHistogram());
        
        tfEditor2D = new TransferFunction2DEditor(volume, gradients);
        tfEditor2D.addTFChangeListener(this);

        System.out.println("Finished initialization of RaycastRenderer");
    }

    public RaycastRendererPanel getPanel() {
        return panel;
    }

    public TransferFunction2DEditor getTF2DPanel() {
        return tfEditor2D;
    }
    
    public TransferFunctionEditor getTFPanel() {
        return tfEditor;
    }
     
    public void setShadingMode(boolean mode) {
        
        shadingMode = mode;
        changed();
    }
    public void setsampleStep(double ss) {
        
        sampleStep = ss;
        changed();
    }
     public void setAmbMode(boolean mode) {
        AmbMode = mode;
        changed();
    }
       public void setDifMode(boolean mode) {
        DifMode = mode;
        changed();
    }
      public void setSpecMode(boolean mode) {
        SpecMode = mode;
        changed();
    }
   
    public void setMIPMode() {
        setMode(false, true, false, false);
    }
    
    public void setSlicerMode() {
        setMode(true, false, false, false);
    }
    
    public void setCompositingMode() {
        setMode(false, false, true, false);
    }
    
    public void setTF2DMode() {
        setMode(false, false, false, true);
    }
    
    private void setMode(boolean slicer, boolean mip, boolean composite, boolean tf2d) {
        slicerMode = slicer;
        mipMode = mip;
        compositingMode = composite;
        tf2dMode= tf2d;        
        changed();
        
        
    }
    
        
    private void drawBoundingBox(GL2 gl) {
        gl.glPushAttrib(GL2.GL_CURRENT_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor4d(1.0, 1.0, 1.0, 1.0);
        gl.glLineWidth(1.5f);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glDisable(GL.GL_LINE_SMOOTH);
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopAttrib();

    }
    
    private boolean intersectLinePlane(double[] plane_pos, double[] plane_normal,
            double[] line_pos, double[] line_dir, double[] intersection) {

        double[] tmp = new double[3];

        for (int i = 0; i < 3; i++) {
            tmp[i] = plane_pos[i] - line_pos[i];
        }

        double denom = VectorMath.dotproduct(line_dir, plane_normal);
        if (Math.abs(denom) < 1.0e-8) {
            return false;
        }

        double t = VectorMath.dotproduct(tmp, plane_normal) / denom;

        for (int i = 0; i < 3; i++) {
            intersection[i] = line_pos[i] + t * line_dir[i];
        }

        return true;
    }

    private boolean validIntersection(double[] intersection, double xb, double xe, double yb,
            double ye, double zb, double ze) {

        return (((xb - 0.5) <= intersection[0]) && (intersection[0] <= (xe + 0.5))
                && ((yb - 0.5) <= intersection[1]) && (intersection[1] <= (ye + 0.5))
                && ((zb - 0.5) <= intersection[2]) && (intersection[2] <= (ze + 0.5)));

    }

    private void intersectFace(double[] plane_pos, double[] plane_normal,
            double[] line_pos, double[] line_dir, double[] intersection,
            double[] entryPoint, double[] exitPoint) {

        boolean intersect = intersectLinePlane(plane_pos, plane_normal, line_pos, line_dir,
                intersection);
        if (intersect) {

            //System.out.println("Plane pos: " + plane_pos[0] + " " + plane_pos[1] + " " + plane_pos[2]);
            //System.out.println("Intersection: " + intersection[0] + " " + intersection[1] + " " + intersection[2]);
            //System.out.println("line_dir * intersection: " + VectorMath.dotproduct(line_dir, plane_normal));

            double xpos0 = 0;
            double xpos1 = volume.getDimX();
            double ypos0 = 0;
            double ypos1 = volume.getDimY();
            double zpos0 = 0;
            double zpos1 = volume.getDimZ();

            if (validIntersection(intersection, xpos0, xpos1, ypos0, ypos1,
                    zpos0, zpos1)) {
                if (VectorMath.dotproduct(line_dir, plane_normal) > 0) {
                    entryPoint[0] = intersection[0];
                    entryPoint[1] = intersection[1];
                    entryPoint[2] = intersection[2];
                } else {
                    exitPoint[0] = intersection[0];
                    exitPoint[1] = intersection[1];
                    exitPoint[2] = intersection[2];
                }
            }
        }
    }
    

      int traceRayMIP(double[] entryPoint, double[] exitPoint, double[] viewVec, double sampleStep) {
        /* to be implemented:  You need to sample the ray and implement the MIP
         * right now it just returns yellow as a color
        */
         
        double total_dis = VectorMath.distance(entryPoint, exitPoint);
        double[] entry_exit_vector = new double[3];
        double[] current_point = new double[3];
        short max_intensity = 0;
        short current_intensity = 0;

        VectorMath.setVector(entry_exit_vector, exitPoint[0]-entryPoint[0], exitPoint[1]-entryPoint[1], exitPoint[2]-entryPoint[2]);
        for (double current_dis = 0; current_dis < total_dis; current_dis += sampleStep){
            for(int i = 0; i<3; i++){
                //current_point[i] = (1 - current_dis/total_dis) * entryPoint[i] + current_dis/total_dis * exitPoint[i];
                current_point[i] = (current_dis/total_dis)*entry_exit_vector[i]+entryPoint[i];
            }
            //VectorMath.setVector(current_point,(current_dis/total_dis)*entry_exit_vector[0]+entryPoint[0],(current_dis/total_dis)*entry_exit_vector[1]+entryPoint[1],(current_dis/total_dis)*entry_exit_vector[2]+entryPoint[2] );
            current_intensity = volume.getVoxelInterpolate(current_point);
            max_intensity = current_intensity > max_intensity ? current_intensity : max_intensity;
        }
        current_intensity = volume.getVoxelInterpolate(exitPoint);
        max_intensity = current_intensity > max_intensity ? current_intensity : max_intensity;
        //System.out.println("max: "+max_intensity);
        int color=0;

        color = (max_intensity << 24)| (255 << 8); 
        
        return color;
        
    }
   
    /**
     * Converts a set of floats between 0 and 1 to an integer colour value in range 0 to 255.
     * @param a alpha channel
     * @param r red channel
     * @param g green channel
     * @param b blue channel
     * @return integer values containing the converted rgba colour.
     */
    private int floatsToColor(float a, float r, float g, float b) {
        int c_alpha = a <= 1.0 ? (int) Math.floor(a * 255) : 255;
        int c_red = r <= 1.0 ? (int) Math.floor(r * 255) : 255;
        int c_green = g <= 1.0 ? (int) Math.floor(g * 255) : 255;
        int c_blue = b <= 1.0 ? (int) Math.floor(b * 255) : 255;
        int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
        return pixelColor;
    }
    private int doublesToColor(double a, double r, double g, double b) {
        int c_alpha = a <= 1.0 ? (int) Math.floor(a * 255) : 255;
        int c_red = r <= 1.0 ? (int) Math.floor(r * 255) : 255;
        int c_green = g <= 1.0 ? (int) Math.floor(g * 255) : 255;
        int c_blue = b <= 1.0 ? (int) Math.floor(b * 255) : 255;
        int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
        return pixelColor;
    }
   
    void computeEntryAndExit(double[] p, double[] viewVec, double[] entryPoint, double[] exitPoint) {

        for (int i = 0; i < 3; i++) {
            entryPoint[i] = -1;
            exitPoint[i] = -1;
        }

        double[] plane_pos = new double[3];
        double[] plane_normal = new double[3];
        double[] intersection = new double[3];

        VectorMath.setVector(plane_pos, volume.getDimX(), 0, 0);
        VectorMath.setVector(plane_normal, 1, 0, 0);
        intersectFace(plane_pos, plane_normal, p, viewVec, intersection, entryPoint, exitPoint);

        VectorMath.setVector(plane_pos, 0, 0, 0);
        VectorMath.setVector(plane_normal, -1, 0, 0);
        intersectFace(plane_pos, plane_normal, p, viewVec, intersection, entryPoint, exitPoint);

        VectorMath.setVector(plane_pos, 0, volume.getDimY(), 0);
        VectorMath.setVector(plane_normal, 0, 1, 0);
        intersectFace(plane_pos, plane_normal, p, viewVec, intersection, entryPoint, exitPoint);

        VectorMath.setVector(plane_pos, 0, 0, 0);
        VectorMath.setVector(plane_normal, 0, -1, 0);
        intersectFace(plane_pos, plane_normal, p, viewVec, intersection, entryPoint, exitPoint);

        VectorMath.setVector(plane_pos, 0, 0, volume.getDimZ());
        VectorMath.setVector(plane_normal, 0, 0, 1);
        intersectFace(plane_pos, plane_normal, p, viewVec, intersection, entryPoint, exitPoint);

        VectorMath.setVector(plane_pos, 0, 0, 0);
        VectorMath.setVector(plane_normal, 0, 0, -1);
        intersectFace(plane_pos, plane_normal, p, viewVec, intersection, entryPoint, exitPoint);

    }

    void raycast(double[] viewMatrix) {
        /* To be partially implemented:
            This function traces the rays through the volume. Have a look and check that you understand how it works.
            You need to introduce here the different modalities MIP/Compositing/TF2/ etc...*/

        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);


        int imageCenter = image.getWidth() / 2;

        double[] pixelCoord = new double[3];
        double[] entryPoint = new double[3];
        double[] exitPoint = new double[3];
        
        int increment=1;
        if(this.interactiveMode){
            increment = increment*2;
        }
        //float sampleStep=1.0f;
        //tfEditor2D.triangleWidget.radius;
        


        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }


        for (int j = 0; j < image.getHeight(); j += increment) {
            for (int i = 0; i < image.getWidth(); i += increment) {
                // compute starting points of rays in a plane shifted backwards to a position behind the data set
                pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter) - viewVec[0] * imageCenter
                        + volume.getDimX() / 2.0;
                pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter) - viewVec[1] * imageCenter
                        + volume.getDimY() / 2.0;
                pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter) - viewVec[2] * imageCenter
                        + volume.getDimZ() / 2.0;

                computeEntryAndExit(pixelCoord, viewVec, entryPoint, exitPoint);
                if ((entryPoint[0] > -1.0) && (exitPoint[0] > -1.0)) {
                    //System.out.println("Entry: " + entryPoint[0] + " " + entryPoint[1] + " " + entryPoint[2]);
                    //System.out.println("Exit: " + exitPoint[0] + " " + exitPoint[1] + " " + exitPoint[2]);
                    int pixelColor = 0;
                                   
                    /* set color to green if MipMode- see slicer function*/
                   if(mipMode && !super.interactiveMode) 
                        pixelColor= traceRayMIP(entryPoint,exitPoint,viewVec,sampleStep);
                   if(compositingMode &&!super.interactiveMode)
                       pixelColor = traceRayCompositingF2B(entryPoint,exitPoint,viewVec,sampleStep);
                   if(tf2dMode &&!super.interactiveMode)
                           pixelColor = traceRaytf2d(entryPoint,exitPoint,viewVec,sampleStep);             
                    for (int ii = i; ii < i + increment; ii++) {
                        for (int jj = j; jj < j + increment; jj++) {
                            image.setRGB(ii, jj, pixelColor);
                        }
                    }
                }

            }
        }


    }

    /**
     * Back-to-front compositing  
     */
    private int traceRayCompositingB2F(double[] entryPoint, double[]exitPoint, double[] viewVec,double sampleStep){
        double total_dis = VectorMath.distance(entryPoint, exitPoint);
        double entry_exit_vector[] = new double[3];
        double current_point[] = new double[3];
        short current_intensity = 0;
        TFColor basic_color = new TFColor(); //the basic color of each point from the TF
        TFColor aug_color = new TFColor(); // the augmented color after using compositing method
        aug_color.a = 1;
        aug_color.r = 0;
        aug_color.g = 0;
        aug_color.b = 0;

        VectorMath.setVector(entry_exit_vector, exitPoint [0]-entryPoint[0], exitPoint[1]-entryPoint[1], exitPoint[2]-entryPoint[2]);
        for (double current_dis = total_dis; current_dis >0; current_dis -= sampleStep){
            for(int i = 0; i<3; i++){
                current_point[i] = (current_dis/total_dis)*entry_exit_vector[i]+entryPoint[i];
            }
            current_intensity = volume.getVoxelInterpolate(current_point);
            basic_color = tFunc.getColor(current_intensity);
            float alpha = (float) (1 - Math.pow(1-basic_color.a, sampleStep)); // true opacity after the interpolation
            aug_color.r = basic_color.r * alpha +(1-alpha)*aug_color.r; // in Back-to-front compositing, the augmented color are composed with two parts:
            aug_color.g = basic_color.g *alpha + (1-alpha)*aug_color.g;  // 1. the basic color multiplied by its opacity just at the point(alpha); 
            aug_color.b = basic_color.b * alpha + (1-alpha)*aug_color.b;  // 2. former color transmitted through the point (1-alpha)
        }
        current_intensity = volume.getVoxelInterpolate(entryPoint);
        basic_color = tFunc.getColor(current_intensity);
        float alpha = (float) (1 - Math.pow(1-basic_color.a, sampleStep));
        aug_color.r = basic_color.r * alpha +(1-alpha)*aug_color.r;
        aug_color.g = basic_color.g *alpha + (1-alpha)*aug_color.g;
        aug_color.b = basic_color.b * alpha + (1-alpha)*aug_color.b;
       // aug_color.a = basic_color.a;

        return doublesToColor(1,aug_color.r,aug_color.g,aug_color.b);

    }


    /**
     * Front-to-back compositing 
     */
    private int traceRayCompositingF2B(double[] entryPoint, double[]exitPoint, double[] viewVec,double sampleStep){
        double total_dis = VectorMath.distance(entryPoint, exitPoint);
        double entry_exit_vector[] = new double[3];
        double current_point[] = new double[3];
        short current_intensity = 0;
        boolean flag = false;
        TFColor basic_color = new TFColor(); 
        TFColor aug_color = new TFColor();
        VoxelGradient current_gradients=new VoxelGradient();
               
        aug_color.a = 0;
        aug_color.r = 0;
        aug_color.g = 0;
        aug_color.b = 0;

        VectorMath.setVector(entry_exit_vector, exitPoint[0]-entryPoint[0], exitPoint[1]-entryPoint[1], exitPoint[2]-entryPoint[2]);
        for (double current_dis = 0; current_dis < total_dis && aug_color.a<0.95; current_dis += sampleStep){
            for(int i = 0; i<3; i++){
                current_point[i] = (current_dis/total_dis)*entry_exit_vector[i]+entryPoint[i];
            }
            if (this.interactiveMode){
                current_intensity = volume.getVoxel((int)Math.floor(current_point[0]),(int)Math.floor(current_point[1]),(int)Math.floor(current_point[2]));
            } else {
                current_intensity = volume.getVoxelInterpolate(current_point);
            }
            

            current_gradients= gradients.getGradient(current_point);
            basic_color.r = tFunc.getColor(current_intensity).r;
            basic_color.a = tFunc.getColor(current_intensity).a;
            basic_color.g = tFunc.getColor(current_intensity).g;
            basic_color.b = tFunc.getColor(current_intensity).b;
            // if(testii<1000){
            //     System.out.println("com: "+basic_color.r);
            //     testii++;
            // }
            if (shadingMode){
          basic_color = shading(current_gradients,basic_color,viewVec);
           } 
            float alpha = (float) (1 - Math.pow(1-basic_color.a, sampleStep));
            aug_color.r += (1-aug_color.a) * basic_color.r *alpha;
            aug_color.g += (1-aug_color.a) * basic_color.g *alpha;
            aug_color.b += (1-aug_color.a) * basic_color.b *alpha;
            aug_color.a += (1-aug_color.a) * alpha;
            flag = (current_dis+sampleStep) >= total_dis ? true :false;
        }
        if (flag){
            current_intensity = volume.getVoxelInterpolate(exitPoint);
            basic_color.r = tFunc.getColor(current_intensity).r;
            basic_color.a = tFunc.getColor(current_intensity).a;
            basic_color.g = tFunc.getColor(current_intensity).g;
            basic_color.b = tFunc.getColor(current_intensity).b;
            float alpha = (float) (1 - Math.pow(1-basic_color.a, sampleStep));
            aug_color.r += (1-aug_color.a) * basic_color.r *alpha;
            aug_color.g += (1-aug_color.a) * basic_color.g *alpha;
            aug_color.b += (1-aug_color.a) * basic_color.b *alpha;
            aug_color.a += (1-aug_color.a) * alpha;
        }
        flag = false;
        return doublesToColor(1,aug_color.r,aug_color.g,aug_color.b);
    }


private TFColor shading(VoxelGradient current_grad, TFColor b_color,double[] viewVec) {
            TFColor Amb_color = new TFColor();//Ia*Ka
            TFColor Dif_color = new TFColor();
            TFColor Spec_color = new TFColor();
            double k_a = 0.1;
            double k_d = 0.7;
            double k_s = 0.2;
            int n = 10;           
            double Ix = current_grad.x;
            //System.out.println(viewVec[0]);
            double Iy = current_grad.y;
           // System.out.println(Ix);
            double Iz = current_grad.z;
            //double Imag = current_grad.mag;
            double Imag = (double) Math.sqrt(Ix*Ix + Iy*Iy + Iz*Iz);
            double Id[] = new double[3];//gradient
            double L[] = new double[3];//Light
            double R[] = new double[3];
            if(Imag-0.0>0.0001){
                VectorMath.setVector(Id,Ix/Imag,Iy/Imag,Iz/Imag);
            } else {
                VectorMath.setVector(Id,0,0,0);
            }
            
            VectorMath.setVector(L,1.0/3.0,2.0/3.0,2.0/3.0);
            //current_intensity = volume.getVoxelInterpolate(current_point);//current_point is used for Light direction
            if(AmbMode == false){ k_a = 0.0;} else {k_a =0.1;}
            if(DifMode == false){ k_d = 0.0;}else {k_d =0.7;}
            if(SpecMode == false){ k_s = 0.0;}else {k_s =0.2;}
            Amb_color.r=  b_color.r * k_a;
            //System.out.println(Amb_color.r);
            Amb_color.g=  b_color.g * k_a;
            Amb_color.b=  b_color.b * k_a;
            
            double cosd=Math.abs(VectorMath.dotproduct(Id, L));
            Dif_color.r=  b_color.r * k_d * cosd;
            Dif_color.g=  b_color.g * k_d * cosd;
            Dif_color.b=  b_color.b * k_d * cosd;
            
            R[0]=cosd*Id[0]*2-L[0];
            R[1]=cosd*Id[1]*2-L[1];
            R[2]=cosd*Id[2]*2-L[2];
            
            double cosa=Math.abs(VectorMath.dotproduct(R, viewVec)/(VectorMath.length(viewVec)*VectorMath.length(R)));
            Spec_color.r=1*k_s* Math.pow(cosa, n);
            Spec_color.g=1*k_s* Math.pow(cosa, n);
            Spec_color.b=1*k_s* Math.pow(cosa, n);
            b_color.r = Amb_color.r+Dif_color.r+Spec_color.r;
            b_color.g =Amb_color.g+Dif_color.g+Spec_color.g;
            b_color.b = Amb_color.b+Dif_color.b+Spec_color.b;
            //b_color.a = 0.5;

            // if(testi<1000){
            //     System.out.println("Viewv: "+VectorMath.length(viewVec)+","+VectorMath.length(R));
            //     System.out.println("Imag: "+Ix+","+Iy+","+Iz+","+Imag);
            //     System.out.println("cosd: "+cosd);
            //     System.out.println("cosa: "+cosa);
            //     System.out.println("a: "+Amb_color.r);
            //     System.out.println("d: "+Dif_color.r);
            //     System.out.println("s: "+Spec_color.r);
            //     testi++;
            // }
            
            
    return b_color;
}
private int traceRaytf2d(double[] entryPoint, double[]exitPoint, double[] viewVec,double sampleStep){
        double total_dis = VectorMath.distance(entryPoint, exitPoint);
        double entry_exit_vector[] = new double[3];
        double current_point[] = new double[3];
        short current_intensity;
        boolean flag = false;
        TFColor basic_color = new TFColor(); 
        TFColor aug_color = new TFColor();
        TFColor set_color = new TFColor();
       
//        TFColor gra_color = new TFColor();
        aug_color.a = 0;
        aug_color.r = 0;
        aug_color.g = 0;
        aug_color.b = 0;
        
        double r=tfEditor2D.triangleWidget.radius;
        int fv=tfEditor2D.triangleWidget.baseIntensity;
        set_color.r=tfEditor2D.triangleWidget.color.r;
        set_color.a=tfEditor2D.triangleWidget.color.a;
        set_color.g=tfEditor2D.triangleWidget.color.g;
        set_color.b=tfEditor2D.triangleWidget.color.b;
        double graMax = tfEditor2D.triangleWidget.graMax;
        double graMin = tfEditor2D.triangleWidget.graMin;
        VectorMath.setVector(entry_exit_vector, exitPoint[0]-entryPoint[0], exitPoint[1]-entryPoint[1], exitPoint[2]-entryPoint[2]);
        for (double current_dis = 0; current_dis < total_dis && aug_color.a<0.95; current_dis += sampleStep){
       // for (double current_dis = 0; current_dis < total_dis; current_dis += sampleStep){

            for(int i = 0; i<3; i++){
                current_point[i] = (current_dis/total_dis)*entry_exit_vector[i]+entryPoint[i];
            }
            current_intensity = volume.getVoxelInterpolate(current_point);
            //System.out.println(+current_intensity);
            //basic_color = tFunc.getColor(current_intensity);
            //if (tf2dMode == false){
                VoxelGradient current_gradients=new VoxelGradient();
                current_gradients= gradients.getGradient(current_point);//,getGradient(x,y,z+1), (float) (coord[2]-z),result_z1);               
  
        
        //current_gradients= gradients.getGradient(current_point);               
                
                if (current_gradients.mag==0&&current_intensity==fv){
                basic_color.a = 1;
                }
                    else if (current_gradients.mag>0 && current_intensity-r*current_gradients.mag<=fv 
                    && current_intensity+r*current_gradients.mag>=fv){
                    basic_color.a = 1-1/r*Math.abs(fv-current_intensity)/current_gradients.mag;                      
                    }
                        else{
                        basic_color.a=0;
                        }
                    basic_color.a=basic_color.a * set_color.a;
                    basic_color.r=set_color.r;
                    basic_color.g=set_color.g;
                    basic_color.b=set_color.b;

            if (current_gradients.mag>graMax || current_gradients.mag<graMin){
                basic_color.a = 0;
            }
            //gradients= 0//;(current_point));
        if (shadingMode){
          basic_color = shading(current_gradients,basic_color,viewVec);
           }   
            
            float alpha = (float) (1 - Math.pow(1-basic_color.a, sampleStep));
            aug_color.r += (1-aug_color.a) * basic_color.r *alpha;
            aug_color.g += (1-aug_color.a) * basic_color.g *alpha;
            aug_color.b += (1-aug_color.a) * basic_color.b *alpha;
            aug_color.a += (1-aug_color.a) * alpha;
            flag = (current_dis+sampleStep) >= total_dis ? true :false;
        }
        if (flag){
            current_intensity = volume.getVoxelInterpolate(exitPoint);
            basic_color.r = tFunc.getColor(current_intensity).r;
            basic_color.a = tFunc.getColor(current_intensity).a;
            basic_color.g = tFunc.getColor(current_intensity).g;
            basic_color.b = tFunc.getColor(current_intensity).b;
            float alpha = (float) (1 - Math.pow(1-basic_color.a, sampleStep));
            aug_color.r += (1-aug_color.a) * basic_color.r *alpha;
            aug_color.g += (1-aug_color.a) * basic_color.g *alpha;
            aug_color.b += (1-aug_color.a) * basic_color.b *alpha;
            aug_color.a += (1-aug_color.a) * alpha;
        }
       
        flag = false;
        //if ()
        return doublesToColor(1,aug_color.r,aug_color.g,aug_color.b);
        
    }


    void slicer(double[] viewMatrix) {

        // clear image
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }

        // vector uVec and vVec define a plane through the origin, 
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);

        // image is square
        int imageCenter = image.getWidth() / 2;

        double[] pixelCoord = new double[3];
        double[] volumeCenter = new double[3];
        VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);

        // sample on a plane through the origin of the volume data
        double max = volume.getMaximum();
        TFColor voxelColor = new TFColor();
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];

                int val = volume.getVoxelInterpolate(pixelCoord);
                // Map the intensity to a grey value by linear scaling
                voxelColor.r = val/max;
                voxelColor.g = voxelColor.r;
                voxelColor.b = voxelColor.r;
                voxelColor.a = val > 0 ? 1.0 : 0.0;  // this makes intensity 0 completely transparent and the rest opaque
                
                // Alternatively, apply the transfer function to obtain a color
                // TFColor basic_color = new TFColor(); 
                // basic_color = tFunc.getColor(val);
                // voxelColor.r=basic_color.r;voxelColor.g=basic_color.g;voxelColor.b=basic_color.b;voxelColor.a=basic_color.a;
                
                
                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
                int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
                int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
                int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
                image.setRGB(i, j, pixelColor);
            }
        }


    }


    @Override
    public void visualize(GL2 gl) {


        if (volume == null) {
            return;
        }

        drawBoundingBox(gl);

        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, viewMatrix, 0);

        long startTime = System.currentTimeMillis();
        if (slicerMode) {
            slicer(viewMatrix);    
        } else {
            raycast(viewMatrix);
        }
        
        long endTime = System.currentTimeMillis();
        double runningTime = (endTime - startTime);
        panel.setSpeedLabel(Double.toString(runningTime));

        Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);

        gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // draw rendered image as a billboard texture
        texture.enable(gl);
        texture.bind(gl);
        double halfWidth = image.getWidth() / 2.0;
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex3d(-halfWidth, -halfWidth, 0.0);
        gl.glTexCoord2d(0.0, 1.0);
        gl.glVertex3d(-halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 1.0);
        gl.glVertex3d(halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 0.0);
        gl.glVertex3d(halfWidth, -halfWidth, 0.0);
        gl.glEnd();
        texture.disable(gl);
        texture.destroy(gl);
        gl.glPopMatrix();

        gl.glPopAttrib();


        if (gl.glGetError() > 0) {
            System.out.println("some OpenGL error: " + gl.glGetError());
        }

    }
    private BufferedImage image;
    private double[] viewMatrix = new double[4 * 4];

    @Override
    public void changed() {
        for (int i=0; i < listeners.size(); i++) {
            listeners.get(i).changed();
        }
    }
}