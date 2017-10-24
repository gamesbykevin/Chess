package com.gamesbykevin.chess.opengl;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.gamesbykevin.androidframeworkv2.base.Entity;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Plane;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector2;
import org.rajawali3d.math.vector.Vector3;

/**
 * Created by Kevin on 10/15/2017.
 */
public class CustomArcballCamera extends ArcballCamera {

    private Context mContext;                       //camera context
    private ScaleGestureDetector mScaleDetector;
    private View.OnTouchListener mGestureListener;
    private GestureDetector mDetector;
    private View mView;                             //camera view
    private boolean mIsRotating;
    private boolean mIsScaling;
    private Vector3 mCameraStartPos;
    private Vector3 mPrevSphereCoord;
    private Vector3 mCurrSphereCoord;
    private Vector2 mPrevScreenCoord;
    private Vector2 mCurrScreenCoord;
    private Quaternion mStartOrientation;
    private Quaternion mCurrentOrientation;
    private Quaternion orientacionAnterior;
    private Object3D mEmpty;
    private Object3D mTarget;                       //camera target object
    private Matrix4 mScratchMatrix;                 //used to retrieve info and for storing mid-results
    private Vector3 mScratchVector;                 //used to retrieve info and for storing mid-results
    private double mStartFOV;
    private float originalX;
    private float originalY;
    private Vector3 xAxis;
    private Vector3 yAxis;
    private Vector3 zAxis;

    //render object reference
    private final BasicRenderer renderer;

    //keep track when we touch down
    private PointF location;

    //how many fingers are touching the screen
    private int fingers = 0;

    /**
     * When touching an object, the user should touch and release the screen in the same range<br>
     * This is the maximum thresh-hold we are allowing for that event to occur
     */
    private static final float TOUCH_MAX = 5.0f;

    /**
     * constructor with no target
     */
    public CustomArcballCamera(BasicRenderer renderer, Context context, View view) {
        this(renderer, context, view, (Object3D) null);
    }

    /**
     * default arcballCamera constructor. sets the principal camera components and
     * adds the camera main listeners
     */
    public CustomArcballCamera(BasicRenderer renderer, Context context, View view, Object3D target) {
        super(context,view,target);
        this.renderer = renderer;
        this.mContext = context;
        this.mTarget = target;
        this.mView = view;
        this.initialize();
        this.addListeners();
    }

    /**initializes de camera components*/
    public void initialize() {
        this.mStartFOV = this.mFieldOfView;
        this.mLookAtEnabled = true;
        this.setLookAt(0.0D, 0.0D, 0.0D);
        this.mEmpty = new Object3D();
        this.mScratchMatrix = new Matrix4();
        this.mScratchVector = new Vector3();
        this.mCameraStartPos = new Vector3();
        this.mPrevSphereCoord = new Vector3();
        this.mCurrSphereCoord = new Vector3();
        this.mPrevScreenCoord = new Vector2();
        this.mCurrScreenCoord = new Vector2();
        this.mStartOrientation = new Quaternion();
        this.mCurrentOrientation = new Quaternion();
        this.xAxis = new Vector3(1,0,0);
        this.yAxis = new Vector3(0,1,0);
        this.zAxis = new Vector3(0,0,1);
        this.location = new PointF();
        this.fingers = 0;
    }

    /**sets the projection matrix*/
    public void setProjectionMatrix(int width, int height) {
        super.setProjectionMatrix(width, height);
    }

    /*with the given x and y coordinates returns a 3D vector with x,y,z sphere coordinates*/
    private void mapToSphere(float x, float y, Vector3 out) {
        float lengthSquared = x * x + y * y;
        if(lengthSquared > 1.0F) {
            out.setAll((double)x, (double)y, 0.0D);
            out.normalize();
        } else {
            out.setAll((double)x, (double)y, Math.sqrt((double)(1.0F - lengthSquared)));
        }
    }

    /**with the given x and y coordinates returns a 2D vector with x,y screen coordinates*/
    private void mapToScreen(float x, float y, Vector2 out) {
        out.setX((double) ((2.0F * x - (float) this.mLastWidth) / (float) this.mLastWidth));
        out.setY((double)(-(2.0F * y - (float)this.mLastHeight) / (float)this.mLastHeight));
    }

    /**maps initial x and y touch event coordinates to <mPrevScreenCoord/> and then copies it to
     * mCurrScreenCoord */
    private void startRotation(float x, float y) {
        this.mapToScreen(x, y, this.mPrevScreenCoord);
        this.mCurrScreenCoord.setAll(this.mPrevScreenCoord.getX(), this.mPrevScreenCoord.getY());
        this.mIsRotating = true;
    }

    /**updates <mCurrScreenCoord/> to new screen mapped x and y and then applies rotation*/
    private void updateRotation(float x, float y) {
        this.mapToScreen(x, y, this.mCurrScreenCoord);
        this.applyRotation();
    }

    /** rotates the sphere? when the rotation move ends it multiplies by the original start orientation
     * accumulates 2 rotations*/
    private void endRotation() {
        this.mStartOrientation.multiply(this.mCurrentOrientation);
    }

    /** applies the rotation to the target object*/
    private void applyRotation() {
        if(this.mIsRotating) {
            //maps to sphere coordinates previous and current position
            this.mapToSphere((float) this.mPrevScreenCoord.getX(), (float) this.mPrevScreenCoord.getY(), this.mPrevSphereCoord);
            this.mapToSphere((float) this.mCurrScreenCoord.getX(), (float) this.mCurrScreenCoord.getY(), this.mCurrSphereCoord);
            //rotationAxis is the crossproduct between the two resultant vectors (normalized)
            Vector3 rotationAxis = this.mPrevSphereCoord.clone();
            rotationAxis.cross(this.mCurrSphereCoord);
            rotationAxis.normalize();
            //rotationAngle is the acos of the vectors' dot product
            double rotationAngle = Math.acos(Math.min(1.0D, this.mPrevSphereCoord.dot(this.mCurrSphereCoord)));
            //creates a quaternion using rotantionAngle and rotationAxis (normalized)
//            this.mCurrentOrientation.fromAngleAxis(rotationAxis.inverse(), MathUtil.radiansToDegrees(-rotationAngle));
//            rotationAxis.setAll(rotationAxis.x,rotationAxis.y,rotationAxis.z);
            this.mCurrentOrientation.fromAngleAxis(rotationAxis.inverse(), MathUtil.radiansToDegrees(-rotationAngle));
            this.mCurrentOrientation.normalize();
            //accumulates start and current orientation in mEmpty object
            Quaternion q = new Quaternion(this.mStartOrientation);
            q.multiply(this.mCurrentOrientation);
            double orientacionX = q.angleBetween(new Quaternion(1f,0f,0f,0f));
            double orientacionY = q.angleBetween(new Quaternion(0f,1f,0f,0f));
            double orientacionZ = q.angleBetween(new Quaternion(0f, 0f, 1f, 0f));
            Log.e("ROTACION","angulo con x "+orientacionX);
            Log.e("ROTACION","angulo con y "+orientacionY);
            Log.e("ROTACION","angulo con z "+orientacionZ);
            q=corregirRotacion(orientacionX,orientacionY,orientacionZ,q);
            this.mEmpty.setOrientation(q);
            this.orientacionAnterior=q;
        }

    }

    /**Corrects the orientation so that the sphere doesn't flip upside down*/
    private Quaternion corregirRotacion(double orientacionX, double orientacionY, double orientacionZ, Quaternion q){
        if((orientacionX > 0.9 && Math.abs(orientacionY-1)<0.2 && orientacionZ>0.75) ||
                (orientacionX > 1 && Math.abs(orientacionY-1)<0.3 && orientacionZ>1.5) ){
//            this.mEmpty.getOrientation(q);  //take the previous orientation
            if(orientacionAnterior!=null){
                q=orientacionAnterior;
                Log.e("ORIENTACION","cambio hecho");
            }
        }
//        return q;
        q.getXAxis();
        q.getYAxis();
        q.getZAxis();
        Log.e("ORIENTACION", "eje x " + q.getXAxis().toString());

        Plane plane = new Plane(yAxis,zAxis,zAxis.inverse());
        Log.e("ORIENTACION", "distancia al eje y "+plane.getDistanceTo(q.getYAxis()));
        return q;
    }

    /**returns the object's view matrix (used in the renderer.Onrender method)*/
    public Matrix4 getViewMatrix() {
        Matrix4 m = super.getViewMatrix();
        //if the camera has a target take its position into mScratchMatrix and store in <m>
        if(this.mTarget != null) {
            this.mScratchMatrix.identity();
            this.mScratchMatrix.translate(this.mTarget.getPosition());
            m.multiply(this.mScratchMatrix);
        }

        //take also orientation from empty object (used in applyRotation) into m
        this.mScratchMatrix.identity();
        this.mScratchMatrix.rotate(this.mEmpty.getOrientation());
        m.multiply(this.mScratchMatrix);
        //if the camera has target take the position inverse and translate to m
        if(this.mTarget != null) {
            this.mScratchVector.setAll(this.mTarget.getPosition());
            this.mScratchVector.inverse();
            this.mScratchMatrix.identity();
            this.mScratchMatrix.translate(this.mScratchVector);
            m.multiply(this.mScratchMatrix);
        }

        return m;
    }


    /** sets the camera's field of view (focal distance)*/
    public void setFieldOfView(double fieldOfView) {
        //lock the frustrum and change the field of view
        Object var3 = this.mFrustumLock;
        synchronized(this.mFrustumLock) {
            this.mStartFOV = fieldOfView;
            super.setFieldOfView(fieldOfView);
        }
    }

    public void removeListeners() {
        CustomArcballCamera.this.mGestureListener = null;
        CustomArcballCamera.this.mScaleDetector = null;
        CustomArcballCamera.this.mDetector = null;
        CustomArcballCamera.this.mView.setOnTouchListener(null);
    }

    /** adds the basic listeners to the camera*/
    public void addListeners() {
        //runs this on the ui thread
        ((Activity)this.mContext).runOnUiThread(new Runnable() {
            public void run() {
                //sets a gesture detector (touch)
                CustomArcballCamera.this.mDetector = new GestureDetector(CustomArcballCamera.this.mContext, CustomArcballCamera.this.new GestureListener());
                //sets a scale detector (zoom)
                CustomArcballCamera.this.mScaleDetector = new ScaleGestureDetector(CustomArcballCamera.this.mContext, CustomArcballCamera.this.new ScaleListener());
                //sets a touch listener
                CustomArcballCamera.this.mGestureListener = new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        //determine what is happening
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {

                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_POINTER_UP:

                                //keep track of how many fingers are touching the screen
                                fingers--;

                                //only 1 finger can select a chess piece
                                if (fingers <= 0) {

                                    //calculate distance
                                    double distance = Entity.getDistance(event.getRawX(), event.getRawY(), location.x, location.y);

                                    if (distance < 0)
                                        distance = -distance;

                                    //call the on touch event in our renderer
                                    if (distance < TOUCH_MAX) {
                                        renderer.onTouchEvent(event);

                                        //don't continue
                                        return true;
                                    }
                                }
                                break;

                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_POINTER_DOWN:

                                //keep track of how many fingers are touching the screen
                                fingers++;

                                //store where we are touching the screen
                                if (fingers == 1) {
                                    location.x = event.getRawX();
                                    location.y = event.getRawY();
                                }
                                break;

                            case MotionEvent.ACTION_MOVE:
                                break;
                        }



                        //sees if it is a scale event
                        CustomArcballCamera.this.mScaleDetector.onTouchEvent(event);
                        if(!CustomArcballCamera.this.mIsScaling) {
                            //if not, delivers the event to the movement detector to start rotation
                            CustomArcballCamera.this.mDetector.onTouchEvent(event);
                            if(event.getAction() == 1 && CustomArcballCamera.this.mIsRotating) {
                                //ends the rotation if the event ended
                                CustomArcballCamera.this.endRotation();
                                CustomArcballCamera.this.mIsRotating = false;
                            }
                        }

                        return true;
                    }
                };
                //sets the touch listener
                CustomArcballCamera.this.mView.setOnTouchListener(CustomArcballCamera.this.mGestureListener);
            }
        });
    }

    /**sets the camera target*/
    public void setTarget(Object3D target) {
        this.mTarget = target;
        this.setLookAt(this.mTarget.getPosition());
    }

    /*returns the camera target*/
    public Object3D getTarget() {
        return this.mTarget;
    }

    /*scale listener*/
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {

        }

        public boolean onScale(ScaleGestureDetector detector) {
            double fov = Math.max(30.0D, Math.min(54.0D, CustomArcballCamera.this.mStartFOV * (1.0D / (double)detector.getScaleFactor())));
            Log.e("SCALE", "detector scale factor "+detector.getScaleFactor());
            CustomArcballCamera.this.setFieldOfView(fov);
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            CustomArcballCamera.this.mIsScaling = true;
            CustomArcballCamera.this.mIsRotating = false;
            return super.onScaleBegin(detector);
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            CustomArcballCamera.this.mIsRotating = false;
            CustomArcballCamera.this.mIsScaling = false;
        }
    }

    /*gesture listener*/
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private GestureListener() {

        }

        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            if(!CustomArcballCamera.this.mIsRotating) {
                CustomArcballCamera.this.originalX=event2.getX();
                CustomArcballCamera.this.originalY=event2.getY();
//                CustomArcballCamera.this.startRotation(event2.getX(), event2.getY());
                CustomArcballCamera.this.startRotation(CustomArcballCamera.this.mLastWidth / 2, CustomArcballCamera.this.mLastHeight / 2); //0,0 es la esquina superior izquierda. Buscar centro camara en algun lugar
                return false;
            } else {
                float x =  (CustomArcballCamera.this.mLastWidth / 2) - (event2.getX() - CustomArcballCamera.this.originalX);
                float y = (CustomArcballCamera.this.mLastHeight / 2) - (event2.getY() - CustomArcballCamera.this.originalY);
                CustomArcballCamera.this.mIsRotating = true;
//                CustomArcballCamera.this.updateRotation(event2.getX(), event2.getY());
                CustomArcballCamera.this.updateRotation(x, y);
                return false;
            }
        }
    }
}