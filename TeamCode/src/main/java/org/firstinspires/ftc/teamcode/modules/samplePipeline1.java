package org.firstinspires.ftc.teamcode.modules;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

public class samplePipeline1 extends OpenCvPipeline {

    private String output = "nothing";

    public samplePipeline1() {

    }

    // Mat is the image matrix that should be processed.
    @Override
    public Mat processFrame(Mat input) {
        Mat hsvMat = input.clone();
        Imgproc.cvtColor(input, hsvMat, Imgproc.COLOR_RGB2HSV);

        Scalar lower = new Scalar(10.588, 145, 90);//15, 75, 40);
        Scalar upper = new Scalar(21.176, 255, 255);//30, 100, 100);

        Mat binaryMat = hsvMat.clone();
        Core.inRange(hsvMat, lower, upper, binaryMat);
        output = "Sample Pipeline Is Running!";
        return binaryMat;
    }

    public String getOutput() {
        return output;
    }
}
